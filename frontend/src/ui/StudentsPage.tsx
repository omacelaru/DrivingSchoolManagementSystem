import { useEffect, useMemo, useState } from "react";
import {
  ApiError,
  createStudent,
  deleteStudent,
  getStudentById,
  getStudentsPage,
  updateStudent,
  type StudentRequestPayload
} from "../api";
import { canDeleteAny, getScopedStudentId, hasAnyRole, isStudentScopedView } from "../authz";
import type { Student } from "../types";

type FormState = {
  firstName: string;
  lastName: string;
  cnp: string;
  email: string;
  phone: string;
  address: string;
  targetDrivingCategoryCodesText: string;
};

const emptyForm: FormState = {
  firstName: "",
  lastName: "",
  cnp: "",
  email: "",
  phone: "",
  address: "",
  targetDrivingCategoryCodesText: "B"
};

function toPayload(form: FormState): StudentRequestPayload {
  return {
    firstName: form.firstName.trim(),
    lastName: form.lastName.trim(),
    cnp: form.cnp.trim(),
    email: form.email.trim(),
    phone: form.phone.trim(),
    address: form.address.trim(),
    targetDrivingCategoryCodes: form.targetDrivingCategoryCodesText
      .split(",")
      .map((code) => code.trim().toUpperCase())
      .filter((code) => code.length > 0)
  };
}

function validateForm(form: FormState): Record<string, string> {
  const errors: Record<string, string> = {};
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const phoneRegex = /^[0-9]{10}$/;
  const cnpRegex = /^[0-9]{13}$/;

  if (!form.firstName.trim()) errors.firstName = "First name is required.";
  if (!form.lastName.trim()) errors.lastName = "Last name is required.";
  if (!cnpRegex.test(form.cnp.trim())) errors.cnp = "CNP must contain exactly 13 digits.";
  if (!emailRegex.test(form.email.trim())) errors.email = "Please enter a valid email address.";
  if (!phoneRegex.test(form.phone.trim())) errors.phone = "Phone must contain exactly 10 digits.";
  if (!form.address.trim()) errors.address = "Address is required.";

  const categories = form.targetDrivingCategoryCodesText
    .split(",")
    .map((value) => value.trim())
    .filter((value) => value.length > 0);
  if (categories.length === 0) {
    errors.targetDrivingCategoryCodesText = "At least one target driving category is required.";
  }

  return errors;
}

function mapApiError(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return error instanceof Error ? error.message : "Unexpected error";
  }
  if (error.status === 409) {
    return "Conflict: student with this CNP or email already exists, or deletion is blocked by active references.";
  }
  if (error.status === 400) {
    return `Validation failed: ${error.message}`;
  }
  return error.message;
}

export function StudentsPage(): JSX.Element {
  const studentScope = isStudentScopedView();
  const scopedStudentId = getScopedStudentId();

  const [students, setStudents] = useState<Student[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [formMode, setFormMode] = useState<"create" | "edit">(studentScope ? "edit" : "create");
  const [editingStudentId, setEditingStudentId] = useState<number | null>(studentScope ? scopedStudentId : null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [formServerMessage, setFormServerMessage] = useState("");
  const writeAllowed = hasAnyRole(["ROLE_ADMIN"]);
  const deleteAllowed = canDeleteAny();

  const query = useMemo(() => {
    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("size", "10");
    params.set("sortBy", "registrationDate");
    params.set("sortDir", "desc");
    return params;
  }, [page]);

  function resetForm(): void {
    if (studentScope && scopedStudentId != null) {
      void getStudentById(scopedStudentId).then((student) => {
        setFormMode("edit");
        setEditingStudentId(student.id);
        setForm({
          firstName: student.firstName,
          lastName: student.lastName,
          cnp: student.cnp,
          email: student.email,
          phone: student.phone,
          address: student.address,
          targetDrivingCategoryCodesText: student.targetDrivingCategoryCodes.join(", ")
        });
      });
      setFormErrors({});
      setFormServerMessage("");
      return;
    }
    setForm(emptyForm);
    setFormErrors({});
    setFormServerMessage("");
    setFormMode("create");
    setEditingStudentId(null);
  }

  function loadStudents(): (() => void) {
    let active = true;
    setLoading(true);
    setError("");

    getStudentsPage(query)
      .then((res) => {
        if (!active) {
          return;
        }
        setStudents(res.items);
        setTotalPages(Math.max(1, res.totalPages));
      })
      .catch((err) => {
        if (!active) {
          return;
        }
        setError(mapApiError(err));
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }

  function loadMyProfile(): (() => void) {
    if (scopedStudentId == null) {
      setError("No student profile is linked to this account.");
      setLoading(false);
      return () => undefined;
    }
    let active = true;
    setLoading(true);
    setError("");
    getStudentById(scopedStudentId)
      .then((student) => {
        if (!active) {
          return;
        }
        setStudents([student]);
        setFormMode("edit");
        setEditingStudentId(student.id);
        setForm({
          firstName: student.firstName,
          lastName: student.lastName,
          cnp: student.cnp,
          email: student.email,
          phone: student.phone,
          address: student.address,
          targetDrivingCategoryCodesText: student.targetDrivingCategoryCodes.join(", ")
        });
      })
      .catch((err) => {
        if (!active) {
          return;
        }
        setError(mapApiError(err));
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }

  useEffect(() => {
    if (studentScope) {
      return loadMyProfile();
    }
  }, [studentScope, scopedStudentId]);

  useEffect(() => {
    if (!studentScope) {
      return loadStudents();
    }
  }, [query, studentScope]);

  function startEdit(student: Student): void {
    setFormMode("edit");
    setEditingStudentId(student.id);
    setFormServerMessage("");
    setFormErrors({});
    setForm({
      firstName: student.firstName,
      lastName: student.lastName,
      cnp: student.cnp,
      email: student.email,
      phone: student.phone,
      address: student.address,
      targetDrivingCategoryCodesText: student.targetDrivingCategoryCodes.join(", ")
    });
  }

  async function handleDelete(studentId: number): Promise<void> {
    const confirmed = window.confirm("Delete this student?");
    if (!confirmed) {
      return;
    }
    try {
      await deleteStudent(studentId);
      setFormServerMessage("Student deleted successfully.");
      loadStudents();
    } catch (err) {
      setFormServerMessage(mapApiError(err));
    }
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    const errors = validateForm(form);
    setFormErrors(errors);
    setFormServerMessage("");
    if (Object.keys(errors).length > 0) {
      return;
    }

    setSubmitting(true);
    try {
      const payload = toPayload(form);
      if (formMode === "create") {
        await createStudent(payload);
      } else if (editingStudentId !== null) {
        await updateStudent(editingStudentId, payload);
      }
      if (studentScope) {
        setFormServerMessage("Profile updated successfully.");
        loadMyProfile();
      } else {
        resetForm();
        setFormServerMessage(formMode === "create" ? "Student created successfully." : "Student updated successfully.");
        loadStudents();
      }
    } catch (err) {
      setFormServerMessage(mapApiError(err));
    } finally {
      setSubmitting(false);
    }
  }

  const formTitle = studentScope
    ? "My profile"
    : formMode === "create"
      ? "Create student"
      : "Edit student";

  return (
    <section className="page">
      <h1>{studentScope ? "My student profile" : "Students"}</h1>

      {writeAllowed && (
        <form className="entity-form" onSubmit={handleSubmit}>
          <h2>{formTitle}</h2>
          <div className="form-grid">
            <label>
              First name
              <input
                value={form.firstName}
                onChange={(e) => setForm((curr) => ({ ...curr, firstName: e.target.value }))}
              />
              {formErrors.firstName && <span className="error">{formErrors.firstName}</span>}
            </label>
            <label>
              Last name
              <input
                value={form.lastName}
                onChange={(e) => setForm((curr) => ({ ...curr, lastName: e.target.value }))}
              />
              {formErrors.lastName && <span className="error">{formErrors.lastName}</span>}
            </label>
            <label>
              CNP
              <input value={form.cnp} onChange={(e) => setForm((curr) => ({ ...curr, cnp: e.target.value }))} />
              {formErrors.cnp && <span className="error">{formErrors.cnp}</span>}
            </label>
            <label>
              Email
              <input value={form.email} onChange={(e) => setForm((curr) => ({ ...curr, email: e.target.value }))} />
              {formErrors.email && <span className="error">{formErrors.email}</span>}
            </label>
            <label>
              Phone
              <input value={form.phone} onChange={(e) => setForm((curr) => ({ ...curr, phone: e.target.value }))} />
              {formErrors.phone && <span className="error">{formErrors.phone}</span>}
            </label>
            <label>
              Address
              <input
                value={form.address}
                onChange={(e) => setForm((curr) => ({ ...curr, address: e.target.value }))}
              />
              {formErrors.address && <span className="error">{formErrors.address}</span>}
            </label>
            <label className="full-width">
              Target license categories (comma separated)
              <input
                value={form.targetDrivingCategoryCodesText}
                onChange={(e) =>
                  setForm((curr) => ({ ...curr, targetDrivingCategoryCodesText: e.target.value }))
                }
                placeholder="B, C"
              />
              {formErrors.targetDrivingCategoryCodesText && (
                <span className="error">{formErrors.targetDrivingCategoryCodesText}</span>
              )}
            </label>
          </div>
          {formServerMessage && <p className="error">{formServerMessage}</p>}
          <div className="form-actions">
            <button className="btn btn-primary" type="submit" disabled={submitting}>
              {submitting ? "Saving..." : formMode === "create" ? "Create" : "Update"}
            </button>
            {!studentScope && formMode === "edit" && (
              <button type="button" className="btn btn-secondary" onClick={resetForm}>
                Cancel edit
              </button>
            )}
            {studentScope && (
              <button type="button" className="btn btn-secondary" onClick={resetForm}>
                Reset to saved
              </button>
            )}
          </div>
        </form>
      )}

      {!studentScope && (
        <>
          <div className="header-line">
            <h2>Student list</h2>
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

          {loading && <p>Loading students...</p>}
          {error && <p className="error">{error}</p>}

          {!loading && !error && (
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {students.map((student) => (
                  <tr key={student.id}>
                    <td>{student.id}</td>
                    <td>
                      {student.firstName} {student.lastName}
                    </td>
                    <td>{student.email}</td>
                    <td>{student.phone}</td>
                    <td>{student.status}</td>
                    <td className="actions-cell">
                      {writeAllowed && (
                        <button type="button" className="btn btn-secondary btn-sm" onClick={() => startEdit(student)}>
                          Edit
                        </button>
                      )}
                      {deleteAllowed && (
                        <button type="button" className="btn btn-danger btn-sm" onClick={() => void handleDelete(student.id)}>
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

      {studentScope && (
        <>
          {loading && <p>Loading profile...</p>}
          {error && <p className="error">{error}</p>}
        </>
      )}
    </section>
  );
}
