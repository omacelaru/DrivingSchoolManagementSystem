import { useEffect, useState } from "react";
import {
  type AdminPaymentFilters,
  ApiError,
  deletePayment,
  getAdminPayments,
  getPaymentById,
  getStudentPayments,
  processPayment,
  refundPayment,
  updatePaymentStatus
} from "../api";
import { getScopedStudentId, isAdmin, isStudentScopedView } from "../authz";
import type { Payment } from "../types";

type ProcessForm = {
  lessonId: string;
  paymentMethod: "CARD" | "CASH" | "BANK_TRANSFER" | "ONLINE";
  transactionId: string;
};

type AdminFilterForm = {
  status: Payment["status"] | "";
  studentId: string;
  lessonId: string;
  paymentMethod: Exclude<Payment["paymentMethod"], null> | "";
  transactionId: string;
  from: string;
  to: string;
};

const emptyProcess: ProcessForm = { lessonId: "", paymentMethod: "CARD", transactionId: "" };
const emptyAdminFilters: AdminFilterForm = {
  status: "",
  studentId: "",
  lessonId: "",
  paymentMethod: "",
  transactionId: "",
  from: "",
  to: ""
};

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
  const adminScope = isAdmin();
  const scopedStudentId = getScopedStudentId();

  const [statusFilter, setStatusFilter] = useState<Payment["status"] | "">("");
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [processForm, setProcessForm] = useState<ProcessForm>(emptyProcess);
  const [adminPaymentId, setAdminPaymentId] = useState("");
  const [adminStatus, setAdminStatus] = useState<Payment["status"]>("CANCELLED");
  const [adminFilters, setAdminFilters] = useState<AdminFilterForm>(emptyAdminFilters);
  const [message, setMessage] = useState("");
  const canManageOwnPayments = studentScope;
  const canManageAsAdmin = adminScope;

  async function loadPayments(): Promise<void> {
    if (!studentScope || scopedStudentId == null) return;
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

  async function loadAdminPayments(): Promise<void> {
    if (!canManageAsAdmin) return;
    const filters: AdminPaymentFilters = {};
    if (adminFilters.status) filters.status = adminFilters.status;
    const studentId = Number(adminFilters.studentId);
    if (Number.isInteger(studentId) && studentId > 0) filters.studentId = studentId;
    const lessonId = Number(adminFilters.lessonId);
    if (Number.isInteger(lessonId) && lessonId > 0) filters.lessonId = lessonId;
    if (adminFilters.paymentMethod) filters.paymentMethod = adminFilters.paymentMethod;
    if (adminFilters.transactionId.trim()) filters.transactionId = adminFilters.transactionId.trim();
    if (adminFilters.from) filters.from = `${adminFilters.from}:00`;
    if (adminFilters.to) filters.to = `${adminFilters.to}:00`;

    setError("");
    setMessage("");
    setLoading(true);
    try {
      const items = await getAdminPayments(filters);
      setPayments(items);
    } catch (err) {
      setError(mapApiError(err));
    } finally {
      setLoading(false);
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

  async function handleLoadPaymentById(): Promise<void> {
    const paymentId = Number(adminPaymentId);
    if (!Number.isInteger(paymentId) || paymentId <= 0) {
      setError("Payment ID must be a positive number.");
      return;
    }
    setError("");
    setMessage("");
    setLoading(true);
    try {
      const payment = await getPaymentById(paymentId);
      setPayments([payment]);
      setMessage("Payment loaded successfully.");
    } catch (err) {
      setError(mapApiError(err));
    } finally {
      setLoading(false);
    }
  }

  async function handleAdminStatusUpdate(): Promise<void> {
    const paymentId = Number(adminPaymentId);
    if (!Number.isInteger(paymentId) || paymentId <= 0) {
      setError("Payment ID must be a positive number.");
      return;
    }
    setError("");
    setMessage("");
    try {
      await updatePaymentStatus(paymentId, adminStatus);
      setMessage("Payment status updated successfully.");
      const updated = await getPaymentById(paymentId);
      setPayments((current) => {
        const exists = current.some((p) => p.id === paymentId);
        if (exists) {
          return current.map((p) => (p.id === paymentId ? updated : p));
        }
        return [updated];
      });
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  async function handleAdminDelete(): Promise<void> {
    const paymentId = Number(adminPaymentId);
    if (!Number.isInteger(paymentId) || paymentId <= 0) {
      setError("Payment ID must be a positive number.");
      return;
    }
    if (!window.confirm("Delete this payment?")) return;
    setError("");
    setMessage("");
    try {
      await deletePayment(paymentId);
      setPayments((current) => current.filter((p) => p.id !== paymentId));
      setMessage("Payment deleted successfully.");
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  async function handleAdminRefund(): Promise<void> {
    const paymentId = Number(adminPaymentId);
    if (!Number.isInteger(paymentId) || paymentId <= 0) {
      setError("Payment ID must be a positive number.");
      return;
    }
    setError("");
    setMessage("");
    try {
      await refundPayment(paymentId);
      setMessage("Payment refunded successfully.");
      const updated = await getPaymentById(paymentId);
      setPayments((current) => {
        const exists = current.some((p) => p.id === paymentId);
        if (exists) {
          return current.map((p) => (p.id === paymentId ? updated : p));
        }
        return [updated];
      });
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  function usePaymentForProcessing(payment: Payment): void {
    if (payment.lessonId == null) {
      setError("This payment has no lesson ID and cannot be used for process form autofill.");
      return;
    }
    setError("");
    setMessage("Process form autofilled from selected payment.");
    setProcessForm((curr) => ({
      ...curr,
      lessonId: String(payment.lessonId)
    }));
  }

  useEffect(() => {
    if (studentScope && scopedStudentId != null) {
      void loadPayments();
    }
    if (canManageAsAdmin) {
      void loadAdminPayments();
    }
  }, [studentScope, scopedStudentId, canManageAsAdmin]);

  return (
    <section className="page">
      <h1>{studentScope ? "My payments" : "Payments admin"}</h1>

      {studentScope && (
        <div className="entity-form">
          <h2>My payments</h2>
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
      )}

      {canManageAsAdmin && (
        <div className="entity-form">
          <h2>Admin payment filters</h2>
          <div className="form-grid">
            <label>
              Status
              <select value={adminFilters.status} onChange={(e) => setAdminFilters((c) => ({ ...c, status: e.target.value as Payment["status"] | "" }))}>
                <option value="">ALL</option>
                <option value="PENDING">PENDING</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="FAILED">FAILED</option>
                <option value="REFUNDED">REFUNDED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </label>
            <label>
              Student ID
              <input value={adminFilters.studentId} onChange={(e) => setAdminFilters((c) => ({ ...c, studentId: e.target.value }))} />
            </label>
            <label>
              Lesson ID
              <input value={adminFilters.lessonId} onChange={(e) => setAdminFilters((c) => ({ ...c, lessonId: e.target.value }))} />
            </label>
            <label>
              Payment method
              <select
                value={adminFilters.paymentMethod}
                onChange={(e) => setAdminFilters((c) => ({ ...c, paymentMethod: e.target.value as AdminFilterForm["paymentMethod"] }))}
              >
                <option value="">ALL</option>
                <option value="CARD">CARD</option>
                <option value="CASH">CASH</option>
                <option value="BANK_TRANSFER">BANK_TRANSFER</option>
                <option value="ONLINE">ONLINE</option>
              </select>
            </label>
            <label>
              Transaction ID
              <input
                value={adminFilters.transactionId}
                onChange={(e) => setAdminFilters((c) => ({ ...c, transactionId: e.target.value }))}
              />
            </label>
            <label>
              From (transaction date)
              <input
                type="datetime-local"
                value={adminFilters.from}
                onChange={(e) => setAdminFilters((c) => ({ ...c, from: e.target.value }))}
              />
            </label>
            <label>
              To (transaction date)
              <input
                type="datetime-local"
                value={adminFilters.to}
                onChange={(e) => setAdminFilters((c) => ({ ...c, to: e.target.value }))}
              />
            </label>
          </div>
          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={() => void loadAdminPayments()} disabled={loading}>
              Apply filters
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => {
                setAdminFilters(emptyAdminFilters);
                setError("");
                setMessage("");
                setLoading(true);
                void getAdminPayments({})
                  .then((items) => setPayments(items))
                  .catch((err) => setError(mapApiError(err)))
                  .finally(() => setLoading(false));
              }}
              disabled={loading}
            >
              Reset filters
            </button>
          </div>
        </div>
      )}

      {canManageAsAdmin && (
        <div className="entity-form">
          <h2>Admin payment tools</h2>
          <div className="form-grid">
            <label>
              Payment ID
              <input value={adminPaymentId} onChange={(e) => setAdminPaymentId(e.target.value)} />
            </label>
            <label>
              Target status
              <select value={adminStatus} onChange={(e) => setAdminStatus(e.target.value as Payment["status"])}>
                <option value="PENDING">PENDING</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="FAILED">FAILED</option>
                <option value="REFUNDED">REFUNDED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </label>
          </div>
          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={() => void handleLoadPaymentById()} disabled={loading}>
              Load payment
            </button>
            <button type="button" className="btn btn-primary" onClick={() => void handleAdminStatusUpdate()}>
              Update status
            </button>
            <button type="button" className="btn btn-danger" onClick={() => void handleAdminDelete()}>
              Delete pending
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => void handleAdminRefund()}>
              Refund payment
            </button>
          </div>
        </div>
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
              <th>Lesson ID</th>
              <th>Transaction ID</th>
              <th>Date</th>
              <th>Notes</th>
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
                <td>{payment.lessonId ?? "-"}</td>
                <td>{payment.transactionId ?? "-"}</td>
                <td>{new Date(payment.transactionDate).toLocaleString()}</td>
                <td>{payment.notes ?? "-"}</td>
                <td className="actions-cell">
                  {canManageOwnPayments && (
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      disabled={payment.lessonId == null || payment.status !== "PENDING"}
                      onClick={() => usePaymentForProcessing(payment)}
                    >
                      Use in process form
                    </button>
                  )}
                  {canManageAsAdmin && (
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => setAdminPaymentId(String(payment.id))}
                    >
                      Select for admin tools
                    </button>
                  )}
                  {!canManageOwnPayments && !canManageAsAdmin && "-"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
