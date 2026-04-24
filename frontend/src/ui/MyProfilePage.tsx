import { useEffect, useMemo, useState } from "react";
import { ApiError, getInstructorById, getStudentById, updateInstructor, updateStudent } from "../api";
import { getScopedInstructorId, getScopedStudentId, isInstructorScopedView, isStudentScopedView } from "../authz";
import type { Instructor, Student } from "../types";

type StudentForm = {
  firstName: string;
  lastName: string;
  cnp: string;
  email: string;
  phone: string;
  address: string;
  targetCategoriesText: string;
  emergencyContactName: string;
  emergencyContactPhone: string;
  notes: string;
};

type InstructorForm = {
  firstName: string;
  lastName: string;
  licenseNumber: string;
  email: string;
  phone: string;
  specialization: "THEORETICAL" | "PRACTICAL" | "BOTH";
};

function mapApiError(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return error instanceof Error ? error.message : "Unexpected error";
  }
  if (error.status === 400) return `Validation failed: ${error.message}`;
  if (error.status === 409) return "Request could not be processed.";
  return error.message;
}

function toStudentForm(student: Student): StudentForm {
  return {
    firstName: student.firstName,
    lastName: student.lastName,
    cnp: student.cnp,
    email: student.email,
    phone: student.phone,
    address: student.address,
    targetCategoriesText: student.targetDrivingCategoryCodes.join(", "),
    emergencyContactName: student.profile?.emergencyContactName ?? "",
    emergencyContactPhone: student.profile?.emergencyContactPhone ?? "",
    notes: student.profile?.notes ?? ""
  };
}

function toInstructorForm(instructor: Instructor): InstructorForm {
  return {
    firstName: instructor.firstName,
    lastName: instructor.lastName,
    licenseNumber: instructor.licenseNumber,
    email: instructor.email,
    phone: instructor.phone,
    specialization: instructor.specialization
  };
}

export function MyProfilePage(): JSX.Element {
  const studentScope = isStudentScopedView();
  const instructorScope = isInstructorScopedView();
  const studentId = getScopedStudentId();
  const instructorId = getScopedInstructorId();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [studentForm, setStudentForm] = useState<StudentForm | null>(null);
  const [instructorForm, setInstructorForm] = useState<InstructorForm | null>(null);

  const profileKind = useMemo(() => {
    if (studentScope) return "student";
    if (instructorScope) return "instructor";
    return "none";
  }, [studentScope, instructorScope]);

  async function loadProfile(): Promise<void> {
    setLoading(true);
    setError("");
    setMessage("");
    try {
      if (profileKind === "student" && studentId != null) {
        const student = await getStudentById(studentId);
        setStudentForm(toStudentForm(student));
        setInstructorForm(null);
        return;
      }
      if (profileKind === "instructor" && instructorId != null) {
        const instructor = await getInstructorById(instructorId);
        setInstructorForm(toInstructorForm(instructor));
        setStudentForm(null);
        return;
      }
      setError("No personal profile is linked to this account.");
      setStudentForm(null);
      setInstructorForm(null);
    } catch (err) {
      setError(mapApiError(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadProfile();
  }, [profileKind, studentId, instructorId]);

  async function handleSaveStudent(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    if (!studentForm || studentId == null) return;
    setSaving(true);
    setError("");
    setMessage("");
    try {
      await updateStudent(studentId, {
        firstName: studentForm.firstName.trim(),
        lastName: studentForm.lastName.trim(),
        cnp: studentForm.cnp.trim(),
        email: studentForm.email.trim(),
        phone: studentForm.phone.trim(),
        address: studentForm.address.trim(),
        targetDrivingCategoryCodes: studentForm.targetCategoriesText
          .split(",")
          .map((code) => code.trim().toUpperCase())
          .filter((code) => code.length > 0),
        profile: {
          emergencyContactName: studentForm.emergencyContactName.trim() || undefined,
          emergencyContactPhone: studentForm.emergencyContactPhone.trim() || undefined,
          notes: studentForm.notes.trim() || undefined
        }
      });
      setMessage("Profile updated.");
      await loadProfile();
    } catch (err) {
      setError(mapApiError(err));
    } finally {
      setSaving(false);
    }
  }

  async function handleSaveInstructor(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    if (!instructorForm || instructorId == null) return;
    setSaving(true);
    setError("");
    setMessage("");
    try {
      await updateInstructor(instructorId, {
        firstName: instructorForm.firstName.trim(),
        lastName: instructorForm.lastName.trim(),
        licenseNumber: instructorForm.licenseNumber.trim().toUpperCase(),
        email: instructorForm.email.trim(),
        phone: instructorForm.phone.trim(),
        specialization: instructorForm.specialization
      });
      setMessage("Profile updated.");
      await loadProfile();
    } catch (err) {
      setError(mapApiError(err));
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="page">
      <h1>My profile</h1>
      {loading && <p>Loading profile...</p>}
      {error && <p className="error">{error}</p>}
      {message && <p className="message-success">{message}</p>}

      {!loading && profileKind === "student" && studentForm && (
        <form className="entity-form" onSubmit={(e) => void handleSaveStudent(e)}>
          <h2>Student profile</h2>
          <div className="form-grid">
            <label>
              First name
              <input value={studentForm.firstName} onChange={(e) => setStudentForm((s) => (s ? { ...s, firstName: e.target.value } : s))} />
            </label>
            <label>
              Last name
              <input value={studentForm.lastName} onChange={(e) => setStudentForm((s) => (s ? { ...s, lastName: e.target.value } : s))} />
            </label>
            <label>
              CNP
              <input value={studentForm.cnp} readOnly />
            </label>
            <label>
              Email
              <input value={studentForm.email} onChange={(e) => setStudentForm((s) => (s ? { ...s, email: e.target.value } : s))} />
            </label>
            <label>
              Phone
              <input value={studentForm.phone} onChange={(e) => setStudentForm((s) => (s ? { ...s, phone: e.target.value } : s))} />
            </label>
            <label>
              Address
              <input value={studentForm.address} onChange={(e) => setStudentForm((s) => (s ? { ...s, address: e.target.value } : s))} />
            </label>
            <label className="full-width">
              Target categories (comma separated)
              <input
                value={studentForm.targetCategoriesText}
                onChange={(e) => setStudentForm((s) => (s ? { ...s, targetCategoriesText: e.target.value } : s))}
              />
            </label>
            <label>
              Emergency contact name
              <input
                value={studentForm.emergencyContactName}
                onChange={(e) => setStudentForm((s) => (s ? { ...s, emergencyContactName: e.target.value } : s))}
              />
            </label>
            <label>
              Emergency contact phone
              <input
                value={studentForm.emergencyContactPhone}
                onChange={(e) => setStudentForm((s) => (s ? { ...s, emergencyContactPhone: e.target.value } : s))}
              />
            </label>
            <label className="full-width">
              Notes
              <input value={studentForm.notes} onChange={(e) => setStudentForm((s) => (s ? { ...s, notes: e.target.value } : s))} />
            </label>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary" type="submit" disabled={saving}>
              {saving ? "Saving..." : "Save changes"}
            </button>
            <button className="btn btn-secondary" type="button" onClick={() => void loadProfile()} disabled={saving}>
              Reset
            </button>
          </div>
        </form>
      )}

      {!loading && profileKind === "instructor" && instructorForm && (
        <form className="entity-form" onSubmit={(e) => void handleSaveInstructor(e)}>
          <h2>Instructor profile</h2>
          <div className="form-grid">
            <label>
              First name
              <input value={instructorForm.firstName} onChange={(e) => setInstructorForm((s) => (s ? { ...s, firstName: e.target.value } : s))} />
            </label>
            <label>
              Last name
              <input value={instructorForm.lastName} onChange={(e) => setInstructorForm((s) => (s ? { ...s, lastName: e.target.value } : s))} />
            </label>
            <label>
              License number
              <input value={instructorForm.licenseNumber} readOnly />
            </label>
            <label>
              Email
              <input value={instructorForm.email} onChange={(e) => setInstructorForm((s) => (s ? { ...s, email: e.target.value } : s))} />
            </label>
            <label>
              Phone
              <input value={instructorForm.phone} onChange={(e) => setInstructorForm((s) => (s ? { ...s, phone: e.target.value } : s))} />
            </label>
            <label>
              Specialization
              <select
                value={instructorForm.specialization}
                onChange={(e) =>
                  setInstructorForm((s) =>
                    s ? { ...s, specialization: e.target.value as InstructorForm["specialization"] } : s
                  )
                }
              >
                <option value="THEORETICAL">THEORETICAL</option>
                <option value="PRACTICAL">PRACTICAL</option>
                <option value="BOTH">BOTH</option>
              </select>
            </label>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary" type="submit" disabled={saving}>
              {saving ? "Saving..." : "Save changes"}
            </button>
            <button className="btn btn-secondary" type="button" onClick={() => void loadProfile()} disabled={saving}>
              Reset
            </button>
          </div>
        </form>
      )}
    </section>
  );
}
