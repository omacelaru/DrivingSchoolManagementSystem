import { useEffect, useState } from "react";
import {
  ApiError,
  createInstructor,
  deleteInstructor,
  getInstructorById,
  getInstructors,
  updateInstructor,
  type InstructorRequestPayload
} from "../api";
import { canDeleteAny, getScopedInstructorId, isInstructorScopedView } from "../authz";
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
  const instructorScope = isInstructorScopedView();
  const scopedInstructorId = getScopedInstructorId();

  const [instructors, setInstructors] = useState<Instructor[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [formMode, setFormMode] = useState<"create" | "edit">(instructorScope ? "edit" : "create");
  const [editingId, setEditingId] = useState<number | null>(instructorScope ? scopedInstructorId : null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [formMessage, setFormMessage] = useState("");
  const deleteAllowed = canDeleteAny();

  function loadInstructors(): void {
    setLoading(true);
    setError("");
    getInstructors()
      .then((list) => setInstructors(list))
      .catch((err) => setError(mapApiError(err)))
      .finally(() => setLoading(false));
  }

  function loadMyProfile(): void {
    if (scopedInstructorId == null) {
      setError("No instructor profile is linked to this account.");
      setLoading(false);
      return;
    }
    setLoading(true);
    setError("");
    getInstructorById(scopedInstructorId)
      .then((instructor) => {
        setInstructors([instructor]);
        setFormMode("edit");
        setEditingId(instructor.id);
        setForm({
          firstName: instructor.firstName,
          lastName: instructor.lastName,
          licenseNumber: instructor.licenseNumber,
          email: instructor.email,
          phone: instructor.phone,
          specialization: instructor.specialization
        });
      })
      .catch((err) => setError(mapApiError(err)))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    if (instructorScope) {
      loadMyProfile();
    } else {
      loadInstructors();
    }
  }, [instructorScope, scopedInstructorId]);

  function resetForm(): void {
    if (instructorScope && scopedInstructorId != null) {
      void getInstructorById(scopedInstructorId).then((instructor) => {
        setFormMode("edit");
        setEditingId(instructor.id);
        setForm({
          firstName: instructor.firstName,
          lastName: instructor.lastName,
          licenseNumber: instructor.licenseNumber,
          email: instructor.email,
          phone: instructor.phone,
          specialization: instructor.specialization
        });
      });
      setFormErrors({});
      setFormMessage("");
      return;
    }
    setForm(emptyForm);
    setFormErrors({});
    setFormMode("create");
    setEditingId(null);
  }

  function startEdit(instructor: Instructor): void {
    setFormMode("edit");
    setEditingId(instructor.id);
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
      loadInstructors();
    } catch (err) {
      setFormMessage(mapApiError(err));
    }
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    const errors = validateForm(form);
    setFormErrors(errors);
    setFormMessage("");
    if (Object.keys(errors).length > 0) {
      return;
    }

    setSubmitting(true);
    try {
      const payload = toPayload(form);
      if (formMode === "create") {
        await createInstructor(payload);
        setFormMessage("Instructor created successfully.");
      } else if (editingId !== null) {
        await updateInstructor(editingId, payload);
        setFormMessage("Instructor updated successfully.");
      }
      if (instructorScope) {
        loadMyProfile();
      } else {
        resetForm();
        loadInstructors();
      }
    } catch (err) {
      setFormMessage(mapApiError(err));
    } finally {
      setSubmitting(false);
    }
  }

  const formTitle = instructorScope
    ? "My profile"
    : formMode === "create"
      ? "Create instructor"
      : "Edit instructor";

  return (
    <section className="page">
      <h1>{instructorScope ? "My instructor profile" : "Instructors"}</h1>

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
        {formMessage && <p className="error">{formMessage}</p>}
        <div className="form-actions">
          <button className="btn btn-primary" type="submit" disabled={submitting}>
            {submitting ? "Saving..." : formMode === "create" ? "Create" : "Update"}
          </button>
          {!instructorScope && formMode === "edit" && (
            <button type="button" className="btn btn-secondary" onClick={resetForm}>
              Cancel edit
            </button>
          )}
          {instructorScope && (
            <button type="button" className="btn btn-secondary" onClick={resetForm}>
              Reset to saved
            </button>
          )}
        </div>
      </form>

      {!instructorScope && (
        <>
          {loading && <p>Loading instructors...</p>}
          {error && <p className="error">{error}</p>}

          {!loading && !error && (
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Specialization</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {instructors.map((instructor) => (
                  <tr key={instructor.id}>
                    <td>{instructor.id}</td>
                    <td>
                      {instructor.firstName} {instructor.lastName}
                    </td>
                    <td>{instructor.email}</td>
                    <td>{instructor.phone}</td>
                    <td>{instructor.specialization}</td>
                    <td className="actions-cell">
                      <button type="button" className="btn btn-secondary btn-sm" onClick={() => startEdit(instructor)}>
                        Edit
                      </button>
                      {deleteAllowed && (
                        <button type="button" className="btn btn-danger btn-sm" onClick={() => void handleDelete(instructor.id)}>
                          Delete
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}

      {instructorScope && (
        <>
          {loading && <p>Loading profile...</p>}
          {error && <p className="error">{error}</p>}
        </>
      )}
    </section>
  );
}
