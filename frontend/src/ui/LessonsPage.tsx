import { useEffect, useState } from "react";
import {
  ApiError,
  createLesson,
  deleteLesson,
  getLessonsByDateRange,
  getLessonsForInstructor,
  getLessonsForStudent,
  updateLesson,
  type LessonRequestPayload
} from "../api";
import {
  canCancelLessons,
  canManageLessons,
  getScopedInstructorId,
  getScopedStudentId,
  isInstructorScopedView,
  isStudentScopedView
} from "../authz";
import type { Lesson } from "../types";

type FormState = {
  courseId: string;
  startTime: string;
  endTime: string;
};

const emptyForm: FormState = {
  courseId: "",
  startTime: "",
  endTime: ""
};

function toIsoFromLocal(localDateTimeValue: string): string {
  return `${localDateTimeValue}:00`;
}

function toLocalInputValue(iso: string): string {
  return iso.slice(0, 16);
}

function defaultRange(): { start: string; end: string } {
  const now = new Date();
  const start = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0);
  const end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 0);
  return {
    start: toLocalInputValue(start.toISOString()),
    end: toLocalInputValue(end.toISOString())
  };
}

function mapApiError(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return error instanceof Error ? error.message : "Unexpected error";
  }
  if (error.status === 409) return "Conflict: lesson slot is not available or operation violates schedule rules.";
  if (error.status === 400) return `Validation failed: ${error.message}`;
  return error.message;
}

function validateForm(form: FormState): Record<string, string> {
  const errors: Record<string, string> = {};
  const courseId = Number(form.courseId);
  if (!Number.isInteger(courseId) || courseId <= 0) errors.courseId = "Course ID must be positive.";
  if (!form.startTime) errors.startTime = "Start time is required.";
  if (form.endTime && new Date(form.endTime) <= new Date(form.startTime)) {
    errors.endTime = "End time must be after start time.";
  }
  return errors;
}

function toPayload(form: FormState): LessonRequestPayload {
  return {
    courseId: Number(form.courseId),
    startTime: toIsoFromLocal(form.startTime),
    endTime: form.endTime ? toIsoFromLocal(form.endTime) : undefined
  };
}

export function LessonsPage(): JSX.Element {
  const studentScope = isStudentScopedView();
  const instructorScope = isInstructorScopedView();
  const scopedStudentId = getScopedStudentId();
  const scopedInstructorId = getScopedInstructorId();

  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [formMode, setFormMode] = useState<"create" | "edit">("create");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [formMessage, setFormMessage] = useState("");
  const [formMessageType, setFormMessageType] = useState<"success" | "error">("success");
  const [range, setRange] = useState(defaultRange());
  const writeAllowed = canManageLessons() && studentScope;
  const cancelAllowed = canCancelLessons();

  function loadLessons(): void {
    setLoading(true);
    setError("");
    if (studentScope && scopedStudentId != null) {
      getLessonsForStudent()
        .then((items) => setLessons(items))
        .catch((err) => setError(mapApiError(err)))
        .finally(() => setLoading(false));
      return;
    }
    if (instructorScope && scopedInstructorId != null) {
      getLessonsForInstructor()
        .then((items) => setLessons(items))
        .catch((err) => setError(mapApiError(err)))
        .finally(() => setLoading(false));
      return;
    }
    getLessonsByDateRange(toIsoFromLocal(range.start), toIsoFromLocal(range.end))
      .then((items) => setLessons(items))
      .catch((err) => setError(mapApiError(err)))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadLessons();
  }, [studentScope, instructorScope, scopedStudentId, scopedInstructorId]);

  function resetForm(): void {
    setForm(emptyForm);
    setFormErrors({});
    setFormMode("create");
    setEditingId(null);
  }

  function startEdit(lesson: Lesson): void {
    setFormMode("edit");
    setEditingId(lesson.id);
    setFormErrors({});
    setForm({
      courseId: String(lesson.courseId ?? ""),
      startTime: toLocalInputValue(lesson.startTime),
      endTime: toLocalInputValue(lesson.endTime)
    });
  }

  async function handleDelete(id: number): Promise<void> {
    if (!window.confirm("Delete/cancel this lesson?")) return;
    try {
      await deleteLesson(id);
      setFormMessage("Lesson deleted successfully.");
      setFormMessageType("success");
      loadLessons();
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
        await createLesson(payload);
        setFormMessage("Lesson created successfully.");
        setFormMessageType("success");
      } else if (editingId !== null) {
        await updateLesson(editingId, payload);
        setFormMessage("Lesson updated successfully.");
        setFormMessageType("success");
      }
      resetForm();
      loadLessons();
    } catch (err) {
      setFormMessage(mapApiError(err));
      setFormMessageType("error");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="page">
      <h1>Lessons</h1>

      {writeAllowed && (
        <form className="entity-form" onSubmit={handleSubmit}>
          <h2>{formMode === "create" ? "Create lesson" : "Edit lesson"}</h2>
          <div className="form-grid">
            <label>
              Course ID
              <input value={form.courseId} onChange={(e) => setForm((c) => ({ ...c, courseId: e.target.value }))} />
              {formErrors.courseId && <span className="error">{formErrors.courseId}</span>}
            </label>
            <label>
              Start time
              <input
                type="datetime-local"
                value={form.startTime}
                onChange={(e) => setForm((c) => ({ ...c, startTime: e.target.value }))}
              />
              {formErrors.startTime && <span className="error">{formErrors.startTime}</span>}
            </label>
            <label>
              End time (optional)
              <input
                type="datetime-local"
                value={form.endTime}
                onChange={(e) => setForm((c) => ({ ...c, endTime: e.target.value }))}
              />
              {formErrors.endTime && <span className="error">{formErrors.endTime}</span>}
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

      {!studentScope && !instructorScope && (
        <div className="entity-form">
          <h2>Filter by date range</h2>
          <div className="form-grid">
            <label>
              Start
              <input
                type="datetime-local"
                value={range.start}
                onChange={(e) => setRange((curr) => ({ ...curr, start: e.target.value }))}
              />
            </label>
            <label>
              End
              <input
                type="datetime-local"
                value={range.end}
                onChange={(e) => setRange((curr) => ({ ...curr, end: e.target.value }))}
              />
            </label>
          </div>
          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={loadLessons} disabled={loading}>
              Refresh list
            </button>
          </div>
        </div>
      )}

      {(studentScope || instructorScope) && (
        <div className="entity-form">
          <h2>{studentScope ? "My lessons" : "My teaching schedule"}</h2>
          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={loadLessons} disabled={loading}>
              Refresh list
            </button>
          </div>
        </div>
      )}

      {loading && <p>Loading lessons...</p>}
      {error && <p className="error">{error}</p>}

      {!loading && !error && (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Student</th>
              <th>Instructor</th>
              <th>Course</th>
              <th>Time</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {lessons.map((lesson) => (
              <tr key={lesson.id}>
                <td>{lesson.id}</td>
                <td>{lesson.studentId}</td>
                <td>{lesson.instructorName}</td>
                <td>{lesson.courseId ?? "-"}</td>
                <td>
                  {lesson.startTime} - {lesson.endTime}
                </td>
                <td>{lesson.status}</td>
                <td className="actions-cell">
                  {writeAllowed && (
                    <button type="button" className="btn btn-secondary btn-sm" onClick={() => startEdit(lesson)}>
                      Edit
                    </button>
                  )}
                  {cancelAllowed && (
                    <button type="button" className="btn btn-danger btn-sm" onClick={() => void handleDelete(lesson.id)}>
                      Cancel
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
