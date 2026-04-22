import { useEffect, useMemo, useState } from "react";
import { ApiError, createCourse, deleteCourse, getCoursesPage, updateCourse, type CourseRequestPayload } from "../api";
import { canDeleteAny, canManageCoursesOrLessons, getScopedInstructorId } from "../authz";
import type { Course } from "../types";

type FormState = {
  name: string;
  description: string;
  price: string;
  instructorId: string;
  vehicleId: string;
  numberOfLessons: string;
  courseType: "THEORETICAL" | "PRACTICAL";
  courseTagCodesText: string;
};

const emptyForm: FormState = {
  name: "",
  description: "",
  price: "",
  instructorId: "",
  vehicleId: "",
  numberOfLessons: "10",
  courseType: "PRACTICAL",
  courseTagCodesText: ""
};

function mapApiError(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return error instanceof Error ? error.message : "Unexpected error";
  }
  if (error.status === 409) return "Conflict: course cannot be created/updated/deleted due to business constraints.";
  if (error.status === 400) return `Validation failed: ${error.message}`;
  return error.message;
}

function validateForm(form: FormState): Record<string, string> {
  const errors: Record<string, string> = {};
  const price = Number(form.price);
  const instructorId = Number(form.instructorId);
  const vehicleId = Number(form.vehicleId);
  const numberOfLessons = Number(form.numberOfLessons);

  if (!form.name.trim()) errors.name = "Course name is required.";
  if (!Number.isFinite(price) || price <= 0) errors.price = "Price must be a positive number.";
  if (!Number.isInteger(instructorId) || instructorId <= 0) errors.instructorId = "Instructor ID must be positive.";
  if (!Number.isInteger(vehicleId) || vehicleId <= 0) errors.vehicleId = "Vehicle ID must be positive.";
  if (!Number.isInteger(numberOfLessons) || numberOfLessons < 1 || numberOfLessons > 100) {
    errors.numberOfLessons = "Number of lessons must be between 1 and 100.";
  }
  return errors;
}

function toPayload(form: FormState): CourseRequestPayload {
  return {
    name: form.name.trim(),
    description: form.description.trim(),
    price: Number(form.price),
    instructorId: Number(form.instructorId),
    vehicleId: Number(form.vehicleId),
    numberOfLessons: Number(form.numberOfLessons),
    courseType: form.courseType,
    courseTagCodes: form.courseTagCodesText
      .split(",")
      .map((code) => code.trim().toUpperCase())
      .filter((code) => code.length > 0)
  };
}

export function CoursesPage(): JSX.Element {
  const [courses, setCourses] = useState<Course[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [formMode, setFormMode] = useState<"create" | "edit">("create");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [formMessage, setFormMessage] = useState("");
  const [formMessageType, setFormMessageType] = useState<"success" | "error">("success");
  const writeAllowed = canManageCoursesOrLessons();
  const deleteAllowed = canDeleteAny();

  const query = useMemo(() => {
    const params = new URLSearchParams();
    const scopedInstructorId = getScopedInstructorId();
    params.set("page", String(page));
    params.set("size", "10");
    params.set("sortBy", "createdAt");
    params.set("sortDir", "desc");
    if (scopedInstructorId != null) {
      params.set("instructorId", String(scopedInstructorId));
    }
    return params;
  }, [page]);

  function loadCourses(): (() => void) {
    let active = true;
    setLoading(true);
    setError("");
    getCoursesPage(query)
      .then((res) => {
        if (!active) return;
        setCourses(res.items);
        setTotalPages(Math.max(1, res.totalPages));
      })
      .catch((err) => {
        if (!active) return;
        setError(mapApiError(err));
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }

  useEffect(() => {
    return loadCourses();
  }, [query]);

  function resetForm(): void {
    setForm(emptyForm);
    setFormErrors({});
    setFormMode("create");
    setEditingId(null);
  }

  function startEdit(course: Course): void {
    setFormMode("edit");
    setEditingId(course.id);
    setFormErrors({});
    setForm({
      name: course.name,
      description: course.description ?? "",
      price: String(course.price),
      instructorId: String(course.instructorId),
      vehicleId: String(course.vehicleId),
      numberOfLessons: String(course.numberOfLessons),
      courseType: course.courseType,
      courseTagCodesText: course.courseTagCodes.join(", ")
    });
  }

  async function handleDelete(id: number): Promise<void> {
    if (!window.confirm("Delete this course?")) return;
    try {
      await deleteCourse(id);
      setFormMessage("Course deleted successfully.");
      setFormMessageType("success");
      loadCourses();
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
    if (Object.keys(errors).length > 0) return;

    setSubmitting(true);
    try {
      const payload = toPayload(form);
      if (formMode === "create") {
        await createCourse(payload);
        setFormMessage("Course created successfully.");
        setFormMessageType("success");
      } else if (editingId !== null) {
        await updateCourse(editingId, payload);
        setFormMessage("Course updated successfully.");
        setFormMessageType("success");
      }
      resetForm();
      loadCourses();
    } catch (err) {
      setFormMessage(mapApiError(err));
      setFormMessageType("error");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="page">
      <h1>Courses</h1>
      {writeAllowed && (
        <form className="entity-form" onSubmit={handleSubmit}>
          <h2>{formMode === "create" ? "Create course" : "Edit course"}</h2>
          <div className="form-grid">
            <label>
              Name
              <input value={form.name} onChange={(e) => setForm((c) => ({ ...c, name: e.target.value }))} />
              {formErrors.name && <span className="error">{formErrors.name}</span>}
            </label>
            <label>
              Price
              <input value={form.price} onChange={(e) => setForm((c) => ({ ...c, price: e.target.value }))} />
              {formErrors.price && <span className="error">{formErrors.price}</span>}
            </label>
            <label>
              Instructor ID
              <input
                value={form.instructorId}
                onChange={(e) => setForm((c) => ({ ...c, instructorId: e.target.value }))}
              />
              {formErrors.instructorId && <span className="error">{formErrors.instructorId}</span>}
            </label>
            <label>
              Vehicle ID
              <input value={form.vehicleId} onChange={(e) => setForm((c) => ({ ...c, vehicleId: e.target.value }))} />
              {formErrors.vehicleId && <span className="error">{formErrors.vehicleId}</span>}
            </label>
            <label>
              Number of lessons
              <input
                value={form.numberOfLessons}
                onChange={(e) => setForm((c) => ({ ...c, numberOfLessons: e.target.value }))}
              />
              {formErrors.numberOfLessons && <span className="error">{formErrors.numberOfLessons}</span>}
            </label>
            <label>
              Course type
              <select
                value={form.courseType}
                onChange={(e) => setForm((c) => ({ ...c, courseType: e.target.value as FormState["courseType"] }))}
              >
                <option value="THEORETICAL">THEORETICAL</option>
                <option value="PRACTICAL">PRACTICAL</option>
              </select>
            </label>
            <label className="full-width">
              Description
              <input
                value={form.description}
                onChange={(e) => setForm((c) => ({ ...c, description: e.target.value }))}
              />
            </label>
            <label className="full-width">
              Course tags (comma separated)
              <input
                value={form.courseTagCodesText}
                onChange={(e) => setForm((c) => ({ ...c, courseTagCodesText: e.target.value }))}
                placeholder="INTENSIVE, WEEKEND"
              />
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

      <div className="header-line">
        <h2>Course list</h2>
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

      {loading && <p>Loading courses...</p>}
      {error && <p className="error">{error}</p>}

      {!loading && !error && (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Type</th>
              <th>Instructor</th>
              <th>Vehicle</th>
              <th>Price</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {courses.map((course) => (
              <tr key={course.id}>
                <td>{course.id}</td>
                <td>{course.name}</td>
                <td>{course.courseType}</td>
                <td>{course.instructorId}</td>
                <td>{course.vehicleId}</td>
                <td>{course.price}</td>
                <td className="actions-cell">
                  {writeAllowed && (
                    <button type="button" className="btn btn-secondary btn-sm" onClick={() => startEdit(course)}>
                      Edit
                    </button>
                  )}
                  {deleteAllowed && (
                    <button type="button" className="btn btn-danger btn-sm" onClick={() => void handleDelete(course.id)}>
                      Delete
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
