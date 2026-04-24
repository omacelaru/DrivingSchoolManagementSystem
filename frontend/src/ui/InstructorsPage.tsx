import { useEffect, useMemo, useState } from "react";
import {
  ApiError,
  createInstructor,
  deleteInstructor,
  getInstructorsPage,
  updateInstructor,
  type InstructorRequestPayload
} from "../api";
import { canCreateInstructorsOrVehicles, canDeleteAny, isAdmin } from "../authz";
import type { Instructor } from "../types";

type FormState = {
  firstName: string;
  lastName: string;
  licenseNumber: string;
  email: string;
  phone: string;
  specialization: "THEORETICAL" | "PRACTICAL" | "BOTH";
};

const emptyForm: FormState = {
  firstName: "",
  lastName: "",
  licenseNumber: "",
  email: "",
  phone: "",
  specialization: "BOTH"
};

function validateForm(form: FormState): Record<string, string> {
  const errors: Record<string, string> = {};
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const phoneRegex = /^[0-9]{10}$/;
  const licenseRegex = /^[A-Z]{2,10}-[0-9]{1,10}$/;

  if (!form.firstName.trim()) errors.firstName = "First name is required.";
  if (!form.lastName.trim()) errors.lastName = "Last name is required.";
  if (!licenseRegex.test(form.licenseNumber.trim().toUpperCase())) {
    errors.licenseNumber = "License number must match PREFIX-NUMBER, e.g. LIC-12345.";
  }
  if (!emailRegex.test(form.email.trim())) errors.email = "Please enter a valid email.";
  if (!phoneRegex.test(form.phone.trim())) errors.phone = "Phone must contain exactly 10 digits.";

  return errors;
}

function mapApiError(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return error instanceof Error ? error.message : "Unexpected error";
  }
  if (error.status === 409) {
    return "Conflict: instructor with this license number or email already exists.";
  }
  if (error.status === 400) {
    return `Validation failed: ${error.message}`;
  }
  return error.message;
}

function toPayload(form: FormState): InstructorRequestPayload {
  return {
    firstName: form.firstName.trim(),
    lastName: form.lastName.trim(),
    licenseNumber: form.licenseNumber.trim().toUpperCase(),
    email: form.email.trim(),
    phone: form.phone.trim(),
    specialization: form.specialization
  };
}

export function InstructorsPage(): JSX.Element {
  const [instructors, setInstructors] = useState<Instructor[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [activeTab, setActiveTab] = useState<"browse" | "manage">("browse");
  const [nameSearch, setNameSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [formMode, setFormMode] = useState<"create" | "edit">("create");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [formMessage, setFormMessage] = useState("");
  const [formMessageType, setFormMessageType] = useState<"success" | "error">("success");
  const writeAllowed = canCreateInstructorsOrVehicles();
  const deleteAllowed = canDeleteAny();
  const showActions = writeAllowed || deleteAllowed;
  const showWorkspace = isAdmin();
  const query = useMemo(() => {
    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("size", "10");
    params.set("sortBy", "createdAt");
    params.set("sortDir", "desc");
    return params;
  }, [page]);

  function loadInstructors(): void {
    setLoading(true);
    setError("");
    getInstructorsPage(query)
      .then((res) => {
        setInstructors(res.items);
        setTotalPages(Math.max(1, res.totalPages));
      })
      .catch((err) => setError(mapApiError(err)))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadInstructors();
  }, [query]);

  function resetForm(): void {
    setForm(emptyForm);
    setFormErrors({});
    setFormMode("create");
    setEditingId(null);
  }

  function startEdit(instructor: Instructor): void {
    setFormMode("edit");
    setEditingId(instructor.id);
    setActiveTab("manage");
    setFormErrors({});
    setForm({
      firstName: instructor.firstName,
      lastName: instructor.lastName,
      licenseNumber: instructor.licenseNumber,
      email: instructor.email,
      phone: instructor.phone,
      specialization: instructor.specialization
    });
  }

  async function handleDelete(id: number): Promise<void> {
    if (!window.confirm("Delete this instructor?")) {
      return;
    }
    try {
      await deleteInstructor(id);
      setFormMessage("Instructor deleted successfully.");
      setFormMessageType("success");
      loadInstructors();
    } catch (err) {
      setFormMessage(mapApiError(err));
      setFormMessageType("error");
    }
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    const errors = validateForm(form);
    setFormErrors(errors);
    setFormMessage("");
    setFormMessageType("success");
    if (Object.keys(errors).length > 0) {
      return;
    }

    setSubmitting(true);
    try {
      const payload = toPayload(form);
      if (formMode === "create") {
        await createInstructor(payload);
        setFormMessage("Instructor created successfully.");
        setFormMessageType("success");
      } else if (editingId !== null) {
        await updateInstructor(editingId, payload);
        setFormMessage("Instructor updated successfully.");
        setFormMessageType("success");
      }
      resetForm();
      loadInstructors();
    } catch (err) {
      setFormMessage(mapApiError(err));
      setFormMessageType("error");
    } finally {
      setSubmitting(false);
    }
  }

  const formTitle = formMode === "create" ? "Create instructor" : "Edit instructor";
  const normalizedNameSearch = nameSearch.trim().toLowerCase();
  const visibleInstructors = normalizedNameSearch.length === 0
    ? instructors
    : instructors.filter((instructor) =>
      `${instructor.firstName} ${instructor.lastName}`.toLowerCase().includes(normalizedNameSearch)
    );

  return (
    <section className="page">
      <h1>Instructors</h1>

      {showWorkspace && (
        <div className="entity-form">
          <h2>Workspace</h2>
          <div className="form-actions">
            <button
              type="button"
              className={activeTab === "browse" ? "btn btn-primary" : "btn btn-secondary"}
              onClick={() => setActiveTab("browse")}
            >
              All instructors
            </button>
            <button
              type="button"
              className={activeTab === "manage" ? "btn btn-primary" : "btn btn-secondary"}
              onClick={() => setActiveTab("manage")}
              disabled={editingId == null}
            >
              Manage instructors
            </button>
          </div>
        </div>
      )}

      {writeAllowed && activeTab === "manage" && (
        <form className="entity-form" onSubmit={handleSubmit}>
          <h2>{formTitle}</h2>
          <div className="form-grid">
            <label>
              First name
              <input value={form.firstName} onChange={(e) => setForm((c) => ({ ...c, firstName: e.target.value }))} />
              {formErrors.firstName && <span className="error">{formErrors.firstName}</span>}
            </label>
            <label>
              Last name
              <input value={form.lastName} onChange={(e) => setForm((c) => ({ ...c, lastName: e.target.value }))} />
              {formErrors.lastName && <span className="error">{formErrors.lastName}</span>}
            </label>
            <label>
              License number
              <input
                value={form.licenseNumber}
                onChange={(e) => setForm((c) => ({ ...c, licenseNumber: e.target.value }))}
              />
              {formErrors.licenseNumber && <span className="error">{formErrors.licenseNumber}</span>}
            </label>
            <label>
              Email
              <input value={form.email} onChange={(e) => setForm((c) => ({ ...c, email: e.target.value }))} />
              {formErrors.email && <span className="error">{formErrors.email}</span>}
            </label>
            <label>
              Phone
              <input value={form.phone} onChange={(e) => setForm((c) => ({ ...c, phone: e.target.value }))} />
              {formErrors.phone && <span className="error">{formErrors.phone}</span>}
            </label>
            <label>
              Specialization
              <select
                value={form.specialization}
                onChange={(e) => setForm((c) => ({ ...c, specialization: e.target.value as FormState["specialization"] }))}
              >
                <option value="THEORETICAL">THEORETICAL</option>
                <option value="PRACTICAL">PRACTICAL</option>
                <option value="BOTH">BOTH</option>
              </select>
            </label>
          </div>
          {formMessage && <p className={formMessageType === "success" ? "message-success" : "error"}>{formMessage}</p>}
          <div className="form-actions">
            <button className="btn btn-primary" type="submit" disabled={submitting}>
              {submitting ? "Saving..." : formMode === "create" ? "Create" : "Update"}
            </button>
            {formMode === "edit" && (
              <button type="button" className="btn btn-secondary" onClick={resetForm}>
                Cancel edit
              </button>
            )}
          </div>
        </form>
      )}

      {activeTab === "browse" && (
        <div className="entity-form">
          <h2>All instructors</h2>
          <div className="form-grid">
            <label>
              Search by name
              <input
                value={nameSearch}
                onChange={(e) => setNameSearch(e.target.value)}
                placeholder="e.g. Mihai Ionescu"
              />
            </label>
          </div>
        </div>
      )}

      {activeTab === "browse" && (
        <div className="header-line">
          <h2>List</h2>
          <div className="pager">
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page <= 0 || loading}
            >
              Prev
            </button>
            <span>
              Page {page + 1} / {totalPages}
            </span>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1 || loading}
            >
              Next
            </button>
          </div>
        </div>
      )}

      {loading && <p>Loading instructors...</p>}
      {error && <p className="error">{error}</p>}

      {!loading && !error && activeTab === "browse" && (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Specialization</th>
              {showActions && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {visibleInstructors.map((instructor) => (
              <tr key={instructor.id}>
                <td>{instructor.id}</td>
                <td>
                  {instructor.firstName} {instructor.lastName}
                </td>
                <td>{instructor.email}</td>
                <td>{instructor.phone}</td>
                <td>{instructor.specialization}</td>
                {showActions && (
                  <td className="actions-cell">
                    {writeAllowed && (
                      <button type="button" className="btn btn-secondary btn-sm" onClick={() => startEdit(instructor)}>
                        Edit
                      </button>
                    )}
                    {deleteAllowed && (
                      <button type="button" className="btn btn-danger btn-sm" onClick={() => void handleDelete(instructor.id)}>
                        Delete
                      </button>
                    )}
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {!loading && !error && activeTab === "browse" && visibleInstructors.length === 0 && (
        <p>No instructors found for this search.</p>
      )}

      {!loading && !error && activeTab === "manage" && (
        <div className="entity-form">
          <h2>Selected instructor</h2>
          {editingId == null ? (
            <p>Select an instructor from <strong>All instructors</strong> and press <strong>Edit</strong>.</p>
          ) : (
            <p>Editing instructor ID: {editingId}</p>
          )}
        </div>
      )}
    </section>
  );
}
