import { useEffect, useMemo, useState } from "react";
import {
  ApiError,
  createStudent,
  deleteStudent,
  getMyStudentProfile,
  getStudentsPage,
  updateStudent,
  updateMyStudentProfile,
  type StudentRequestPayload
} from "../api";
import { canDeleteAny, getScopedStudentId, hasAnyRole, isStudentScopedView } from "../authz";
import {
  DRIVING_LICENSE_CATEGORY_CODES,
  expandDrivingCategories,
  LICENSE_CATEGORY_HINTS,
  type DrivingLicenseCategoryCode
} from "../constants/drivingLicenseCategories";
import type { Student } from "../types";

type FormState = {
  firstName: string;
  lastName: string;
  cnp: string;
  email: string;
  phone: string;
  address: string;
};

const emptyForm: FormState = {
  firstName: "",
  lastName: "",
  cnp: "",
  email: "",
  phone: "",
  address: ""
};

function toggleInSet(set: Set<DrivingLicenseCategoryCode>, code: DrivingLicenseCategoryCode): Set<DrivingLicenseCategoryCode> {
  const next = new Set(set);
  if (next.has(code)) {
    next.delete(code);
  } else {
    next.add(code);
  }
  return next;
}

function toCategorySelection(codes: string[]): Set<DrivingLicenseCategoryCode> {
  return new Set(
    codes.filter((code): code is DrivingLicenseCategoryCode =>
      DRIVING_LICENSE_CATEGORY_CODES.includes(code as DrivingLicenseCategoryCode)
    )
  );
}

function toPayload(form: FormState, expandedCategories: Set<DrivingLicenseCategoryCode>): StudentRequestPayload {
  return {
    firstName: form.firstName.trim(),
    lastName: form.lastName.trim(),
    cnp: form.cnp.trim(),
    email: form.email.trim(),
    phone: form.phone.trim(),
    address: form.address.trim(),
    targetDrivingCategoryCodes: DRIVING_LICENSE_CATEGORY_CODES.filter((code) => expandedCategories.has(code))
  };
}

function validateForm(form: FormState, expandedCategories: Set<DrivingLicenseCategoryCode>): Record<string, string> {
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

  if (expandedCategories.size === 0) {
    errors.targetDrivingCategoryCodes = "At least one target driving category is required.";
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
  const [activeTab, setActiveTab] = useState<"browse" | "manage">("browse");
  const [nameSearch, setNameSearch] = useState("");
  const [editingStudentId, setEditingStudentId] = useState<number | null>(studentScope ? scopedStudentId : null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [selectedCategories, setSelectedCategories] = useState<Set<DrivingLicenseCategoryCode>>(() => new Set(["B"]));
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [formServerMessage, setFormServerMessage] = useState("");
  const [formServerMessageType, setFormServerMessageType] = useState<"success" | "error">("success");
  const writeAllowed = studentScope || hasAnyRole(["ROLE_ADMIN"]);
  const deleteAllowed = canDeleteAny();

  const query = useMemo(() => {
    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("size", "10");
    params.set("sortBy", "registrationDate");
    params.set("sortDir", "desc");
    return params;
  }, [page]);
  const expandedCategories = useMemo(() => expandDrivingCategories(selectedCategories), [selectedCategories]);

  function resetForm(): void {
    if (studentScope && scopedStudentId != null) {
      void getMyStudentProfile().then((student) => {
        setFormMode("edit");
        setEditingStudentId(student.id);
        setForm({
          firstName: student.firstName,
          lastName: student.lastName,
          cnp: student.cnp,
          email: student.email,
          phone: student.phone,
          address: student.address
        });
        setSelectedCategories(toCategorySelection(student.targetDrivingCategoryCodes));
      });
      setFormErrors({});
      setFormServerMessage("");
      setFormServerMessageType("success");
      return;
    }
    setForm(emptyForm);
    setFormErrors({});
    setFormServerMessage("");
    setFormServerMessageType("success");
    setFormMode("create");
    setEditingStudentId(null);
    setSelectedCategories(new Set(["B"]));
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
    getMyStudentProfile()
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
          address: student.address
        });
        setSelectedCategories(toCategorySelection(student.targetDrivingCategoryCodes));
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
    if (!studentScope) {
      setActiveTab("manage");
    }
    setFormServerMessage("");
    setFormServerMessageType("success");
    setFormErrors({});
    setForm({
      firstName: student.firstName,
      lastName: student.lastName,
      cnp: student.cnp,
      email: student.email,
      phone: student.phone,
      address: student.address
    });
    setSelectedCategories(toCategorySelection(student.targetDrivingCategoryCodes));
  }

  async function handleDelete(studentId: number): Promise<void> {
    const confirmed = window.confirm("Delete this student?");
    if (!confirmed) {
      return;
    }
    try {
      await deleteStudent(studentId);
      setFormServerMessage("Student deleted successfully.");
      setFormServerMessageType("success");
      loadStudents();
    } catch (err) {
      setFormServerMessage(mapApiError(err));
      setFormServerMessageType("error");
    }
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    const errors = validateForm(form, expandedCategories);
    setFormErrors(errors);
    setFormServerMessage("");
    setFormServerMessageType("success");
    if (Object.keys(errors).length > 0) {
      return;
    }

    setSubmitting(true);
    try {
      const payload = toPayload(form, expandedCategories);
      if (formMode === "create") {
        await createStudent(payload);
      } else if (studentScope) {
        await updateMyStudentProfile(payload);
      } else if (editingStudentId !== null) {
        await updateStudent(editingStudentId, payload);
      }
      if (studentScope) {
        setFormServerMessage("Profile updated successfully.");
        setFormServerMessageType("success");
        loadMyProfile();
      } else {
        resetForm();
        setFormServerMessage(formMode === "create" ? "Student created successfully." : "Student updated successfully.");
        setFormServerMessageType("success");
        loadStudents();
      }
    } catch (err) {
      setFormServerMessage(mapApiError(err));
      setFormServerMessageType("error");
    } finally {
      setSubmitting(false);
    }
  }

  const formTitle = studentScope
    ? "My profile"
    : formMode === "create"
      ? "Create student"
      : "Edit student";
  const normalizedNameSearch = nameSearch.trim().toLowerCase();
  const visibleStudents = normalizedNameSearch.length === 0
    ? students
    : students.filter((student) => `${student.firstName} ${student.lastName}`.toLowerCase().includes(normalizedNameSearch));

  return (
    <section className="page">
      <h1>{studentScope ? "My student profile" : "Students"}</h1>

      {!studentScope && (
        <div className="entity-form">
          <h2>Workspace</h2>
          <div className="form-actions">
            <button
              type="button"
              className={activeTab === "browse" ? "btn btn-primary" : "btn btn-secondary"}
              onClick={() => setActiveTab("browse")}
            >
              All students
            </button>
            <button
              type="button"
              className={activeTab === "manage" ? "btn btn-primary" : "btn btn-secondary"}
              onClick={() => setActiveTab("manage")}
              disabled={editingStudentId == null}
            >
              Manage students
            </button>
          </div>
        </div>
      )}

      {writeAllowed && (studentScope || activeTab === "manage") && (
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
            <fieldset className="full-width category-fieldset">
              <legend className="register-label">Target driving licence categories</legend>
              <p className="field-hint">Select all categories you want this student to train for.</p>
              <div className="category-grid">
                {DRIVING_LICENSE_CATEGORY_CODES.map((code) => {
                  const hint = LICENSE_CATEGORY_HINTS[code];
                  const id = `student-cat-${code}`;
                  const explicitlySelected = selectedCategories.has(code);
                  const autoIncluded = expandedCategories.has(code) && !explicitlySelected;
                  return (
                    <label key={code} htmlFor={id} className="category-card">
                      <input
                        id={id}
                        type="checkbox"
                        checked={expandedCategories.has(code)}
                        disabled={autoIncluded}
                        onChange={() => setSelectedCategories((prev) => toggleInSet(prev, code))}
                      />
                      <span className="category-card-body">
                        <span className="category-code">{code}</span>
                        <span className="category-hint">{hint}</span>
                      </span>
                    </label>
                  );
                })}
              </div>
              {formErrors.targetDrivingCategoryCodes && <span className="error">{formErrors.targetDrivingCategoryCodes}</span>}
            </fieldset>
          </div>
          {formServerMessage && (
            <p className={formServerMessageType === "success" ? "message-success" : "error"}>
              {formServerMessage}
            </p>
          )}
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

      {!studentScope && activeTab === "browse" && (
        <>
          <div className="entity-form">
            <h2>All students</h2>
            <div className="form-grid">
              <label>
                Search by name
                <input
                  value={nameSearch}
                  onChange={(e) => setNameSearch(e.target.value)}
                  placeholder="e.g. Ana Popescu"
                />
              </label>
            </div>
          </div>
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
                {visibleStudents.map((student) => (
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
          {!loading && !error && visibleStudents.length === 0 && <p>No students found for this search.</p>}
        </>
      )}

      {!studentScope && activeTab === "manage" && (
        <>
          {editingStudentId == null ? (
            <div className="entity-form">
              <h2>Manage student</h2>
              <p>Select a student from <strong>All students</strong> and press <strong>Edit</strong>.</p>
            </div>
          ) : (
            <div className="entity-form">
              <h2>Selected student</h2>
              <p>ID: {editingStudentId}</p>
              <p>Use the form above to update this student.</p>
            </div>
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
