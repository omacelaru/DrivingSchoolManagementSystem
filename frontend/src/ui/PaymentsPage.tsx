import { useEffect, useState } from "react";
import {
  ApiError,
  createPendingPayment,
  getStudentPayments,
  processPayment
} from "../api";
import { getScopedStudentId, isStudentScopedView } from "../authz";
import type { Payment } from "../types";

type PendingForm = {
  amount: string;
  lessonId: string;
  notes: string;
};

type ProcessForm = {
  lessonId: string;
  paymentMethod: "CARD" | "CASH" | "BANK_TRANSFER" | "ONLINE";
  transactionId: string;
};

const emptyPending: PendingForm = { amount: "", lessonId: "", notes: "" };
const emptyProcess: ProcessForm = { lessonId: "", paymentMethod: "CARD", transactionId: "" };

function mapApiError(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return error instanceof Error ? error.message : "Unexpected error";
  }
  if (error.status === 409) return "Conflict: payment state does not allow this operation.";
  if (error.status === 400) return `Validation failed: ${error.message}`;
  return error.message;
}

export function PaymentsPage(): JSX.Element {
  const studentScope = isStudentScopedView();
  const scopedStudentId = getScopedStudentId();

  const [statusFilter, setStatusFilter] = useState<Payment["status"] | "">("");
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [pendingForm, setPendingForm] = useState<PendingForm>(emptyPending);
  const [processForm, setProcessForm] = useState<ProcessForm>(emptyProcess);
  const [message, setMessage] = useState("");
  const canManageOwnPayments = studentScope;

  async function loadPayments(): Promise<void> {
    setError("");
    setMessage("");
    setLoading(true);
    try {
      const items = await getStudentPayments(statusFilter || undefined);
      setPayments(items);
    } catch (err) {
      setError(mapApiError(err));
    } finally {
      setLoading(false);
    }
  }

  async function handleCreatePending(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    setMessage("");
    setError("");
    const amount = Number(pendingForm.amount);
    const lessonId = pendingForm.lessonId ? Number(pendingForm.lessonId) : undefined;
    if (!Number.isFinite(amount) || amount <= 0) {
      setError("Pending payment needs a positive amount.");
      return;
    }
    try {
      await createPendingPayment({
        amount,
        lessonId: Number.isInteger(lessonId) && lessonId! > 0 ? lessonId : undefined,
        notes: pendingForm.notes.trim() || undefined
      });
      setPendingForm(emptyPending);
      setMessage("Pending payment created successfully.");
      await loadPayments();
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  async function handleProcessPayment(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    setMessage("");
    setError("");
    const lessonId = Number(processForm.lessonId);
    if (!Number.isInteger(lessonId) || lessonId <= 0) {
      setError("Process payment needs a valid lesson ID.");
      return;
    }
    try {
      await processPayment({
        lessonId,
        paymentMethod: processForm.paymentMethod,
        transactionId: processForm.transactionId.trim() || undefined
      });
      setProcessForm(emptyProcess);
      setMessage("Payment processed successfully.");
      await loadPayments();
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  useEffect(() => {
    if (studentScope && scopedStudentId != null) {
      void loadPayments();
    }
  }, [studentScope, scopedStudentId]);

  return (
    <section className="page">
      <h1>{studentScope ? "My payments" : "Payments"}</h1>

      <div className="entity-form">
        <h2>{studentScope ? "My payments" : "List payments"}</h2>
        <div className="form-grid">
          <label>
            Status filter (optional)
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value as Payment["status"] | "")}>
              <option value="">ALL</option>
              <option value="PENDING">PENDING</option>
              <option value="COMPLETED">COMPLETED</option>
              <option value="FAILED">FAILED</option>
              <option value="REFUNDED">REFUNDED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </label>
        </div>
        <div className="form-actions">
          <button type="button" className="btn btn-secondary" onClick={() => void loadPayments()} disabled={loading}>
            Load payments
          </button>
        </div>
      </div>

      {canManageOwnPayments && (
      <form className="entity-form" onSubmit={handleCreatePending}>
        <h2>Create pending payment</h2>
        <div className="form-grid">
          <label>
            Amount
            <input value={pendingForm.amount} onChange={(e) => setPendingForm((c) => ({ ...c, amount: e.target.value }))} />
          </label>
          <label>
            Lesson ID (optional)
            <input value={pendingForm.lessonId} onChange={(e) => setPendingForm((c) => ({ ...c, lessonId: e.target.value }))} />
          </label>
          <label>
            Notes
            <input value={pendingForm.notes} onChange={(e) => setPendingForm((c) => ({ ...c, notes: e.target.value }))} />
          </label>
        </div>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary">
            Create pending
          </button>
        </div>
      </form>
      )}

      {canManageOwnPayments && (
      <form className="entity-form" onSubmit={handleProcessPayment}>
        <h2>Process payment</h2>
        <div className="form-grid">
          <label>
            Lesson ID
            <input value={processForm.lessonId} onChange={(e) => setProcessForm((c) => ({ ...c, lessonId: e.target.value }))} />
          </label>
          <label>
            Payment method
            <select value={processForm.paymentMethod} onChange={(e) => setProcessForm((c) => ({ ...c, paymentMethod: e.target.value as ProcessForm["paymentMethod"] }))}>
              <option value="CARD">CARD</option>
              <option value="CASH">CASH</option>
              <option value="BANK_TRANSFER">BANK_TRANSFER</option>
              <option value="ONLINE">ONLINE</option>
            </select>
          </label>
          <label>
            Transaction ID (optional)
            <input value={processForm.transactionId} onChange={(e) => setProcessForm((c) => ({ ...c, transactionId: e.target.value }))} />
          </label>
        </div>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary">
            Process payment
          </button>
        </div>
      </form>
      )}

      {message && <p className="message-success">{message}</p>}
      {error && <p className="error">{error}</p>}

      {loading && <p>Loading payments...</p>}
      {!loading && payments.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Student</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Method</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {payments.map((payment) => (
              <tr key={payment.id}>
                <td>{payment.id}</td>
                <td>{payment.studentId}</td>
                <td>{payment.amount}</td>
                <td>{payment.status}</td>
                <td>{payment.paymentMethod ?? "-"}</td>
                <td className="actions-cell">-</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
