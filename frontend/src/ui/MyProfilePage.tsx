import { useEffect, useMemo, useState } from "react";
import { ApiError, getMyInstructorProfile, getMyStudentProfile, updateMyInstructorProfile, updateMyStudentProfile } from "../api";
import { isInstructorScopedView, isStudentScopedView } from "../authz";
import type { Instructor, Student } from "../types";

type StudentForm = {
  firstName: string;
  lastName: string;
  cnp: string;
  email: string;
  phone: string;
  address: string;
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
    phone: instructor.phone
  };
}

export function MyProfilePage(): JSX.Element {
  const studentScope = isStudentScopedView();
  const instructorScope = isInstructorScopedView();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [studentForm, setStudentForm] = useState<StudentForm | null>(null);
  const [instructorForm, setInstructorForm] = useState<InstructorForm | null>(null);
  const [studentSnapshot, setStudentSnapshot] = useState<Student | null>(null);
  const [instructorSnapshot, setInstructorSnapshot] = useState<Instructor | null>(null);

  const profileKind = useMemo(() => {
    if (studentScope) return "student";
    if (instructorScope) return "instructor";
    return "none";
  }, [studentScope, instructorScope]);

  async function loadProfile(options?: { keepMessage?: boolean }): Promise<void> {
    setLoading(true);
    setError("");
    if (!options?.keepMessage) {
      setMessage("");
    }
    try {
      if (profileKind === "student") {
        const student = await getMyStudentProfile();
        setStudentSnapshot(student);
        setInstructorSnapshot(null);
        setStudentForm(toStudentForm(student));
        setInstructorForm(null);
        return;
      }
      if (profileKind === "instructor") {
        const instructor = await getMyInstructorProfile();
        setInstructorSnapshot(instructor);
        setStudentSnapshot(null);
        setInstructorForm(toInstructorForm(instructor));
        setStudentForm(null);
        return;
      }
      setError("No personal profile is linked to this account.");
      setStudentForm(null);
      setInstructorForm(null);
      setStudentSnapshot(null);
      setInstructorSnapshot(null);
    } catch (err) {
      setError(mapApiError(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadProfile();
  }, [profileKind]);

  async function handleSaveStudent(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    if (!studentForm) return;
    setSaving(true);
    setError("");
    setMessage("");
    try {
      await updateMyStudentProfile({
        firstName: studentForm.firstName.trim(),
        lastName: studentForm.lastName.trim(),
        phone: studentForm.phone.trim(),
        address: studentForm.address.trim(),
        profile: {
          emergencyContactName: studentForm.emergencyContactName.trim() || undefined,
          emergencyContactPhone: studentForm.emergencyContactPhone.trim() || undefined,
          notes: studentForm.notes.trim() || undefined
        }
      });
      setMessage("Profile updated.");
      await loadProfile({ keepMessage: true });
    } catch (err) {
      setError(mapApiError(err));
    } finally {
      setSaving(false);
    }
  }

  async function handleSaveInstructor(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    if (!instructorForm) return;
    setSaving(true);
    setError("");
    setMessage("");
    try {
      await updateMyInstructorProfile({
        firstName: instructorForm.firstName.trim(),
        lastName: instructorForm.lastName.trim(),
        phone: instructorForm.phone.trim()
      });
      setMessage("Profile updated.");
      await loadProfile({ keepMessage: true });
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

      {!loading && profileKind === "student" && studentForm && studentSnapshot && (
        <form className="entity-form" onSubmit={(e) => void handleSaveStudent(e)}>
          <h2>Student profile</h2>
          <div className="profile-section profile-section-readonly">
            <div className="profile-section-header">
              <h3>Read-only fields</h3>
            </div>
            <div className="form-grid">
              <label>
                CNP
                <input value={studentForm.cnp} readOnly />
              </label>
              <label>
                Email
                <input value={studentForm.email} readOnly />
              </label>
              <label className="full-width">
                Target categories
                <input
                  value={studentSnapshot.targetDrivingCategoryCodes.join(", ")}
                  readOnly
                />
              </label>
            </div>
          </div>
          <div className="profile-section">
            <div className="profile-section-header">
              <h3>Editable fields</h3>
            </div>
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
                Phone
                <input value={studentForm.phone} onChange={(e) => setStudentForm((s) => (s ? { ...s, phone: e.target.value } : s))} />
              </label>
              <label>
                Address
                <input value={studentForm.address} onChange={(e) => setStudentForm((s) => (s ? { ...s, address: e.target.value } : s))} />
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

      {!loading && profileKind === "instructor" && instructorForm && instructorSnapshot && (
        <form className="entity-form" onSubmit={(e) => void handleSaveInstructor(e)}>
          <h2>Instructor profile</h2>
          <div className="profile-section profile-section-readonly">
            <div className="profile-section-header">
              <h3>Read-only fields</h3>
            </div>
            <div className="form-grid">
              <label>
                License number
                <input value={instructorForm.licenseNumber} readOnly />
              </label>
              <label>
                Email
                <input value={instructorForm.email} readOnly />
              </label>
              <label>
                Specialization
                <input value={instructorSnapshot.specialization} readOnly />
              </label>
            </div>
          </div>
          <div className="profile-section">
            <div className="profile-section-header">
              <h3>Editable fields</h3>
            </div>
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
                Phone
                <input value={instructorForm.phone} onChange={(e) => setInstructorForm((s) => (s ? { ...s, phone: e.target.value } : s))} />
              </label>
            </div>
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
