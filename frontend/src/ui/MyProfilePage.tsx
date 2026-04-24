import { useEffect, useMemo, useState } from "react";
import {
  ApiError,
  deleteMyStudentDocument,
  getMyInstructorProfile,
  getMyStudentDocuments,
  getMyStudentProfile,
  updateMyInstructorProfile,
  updateMyStudentDocument,
  updateMyStudentProfile,
  uploadMyStudentDocument
} from "../api";
import { isInstructorScopedView, isStudentScopedView } from "../authz";
import type { Instructor, Student, StudentDocument } from "../types";

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

const DOCUMENT_TYPES: StudentDocument["documentType"][] = [
  "ID_COPY",
  "PHOTO",
  "MEDICAL_CERTIFICATE",
  "DRIVING_LICENSE_COPY"
];

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
  const [documents, setDocuments] = useState<StudentDocument[]>([]);
  const [documentsLoading, setDocumentsLoading] = useState(false);
  const [documentsBusy, setDocumentsBusy] = useState(false);
  const [uploadType, setUploadType] = useState<StudentDocument["documentType"]>("ID_COPY");
  const [uploadPath, setUploadPath] = useState("");
  const [editingDocumentId, setEditingDocumentId] = useState<number | null>(null);
  const [documentsError, setDocumentsError] = useState("");

  async function loadDocuments(options?: { keepMessage?: boolean }): Promise<void> {
    setDocumentsLoading(true);
    setDocumentsError("");
    if (!options?.keepMessage) {
      setMessage("");
    }
    try {
      const result = await getMyStudentDocuments();
      setDocuments(result);
    } catch (err) {
      setDocumentsError(mapApiError(err));
    } finally {
      setDocumentsLoading(false);
    }
  }

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
        const docs = await getMyStudentDocuments();
        setStudentSnapshot(student);
        setInstructorSnapshot(null);
        setStudentForm(toStudentForm(student));
        setInstructorForm(null);
        setDocuments(docs);
        setDocumentsError("");
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
      setDocuments([]);
      setDocumentsError("");
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

  async function handleSubmitDocument(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    const trimmedPath = uploadPath.trim();
    if (!trimmedPath) {
      setDocumentsError("File path is required.");
      return;
    }
    setDocumentsBusy(true);
    setDocumentsError("");
    setError("");
    setMessage("");
    try {
      if (editingDocumentId == null) {
        await uploadMyStudentDocument(uploadType, trimmedPath);
        setMessage("Document uploaded successfully.");
      } else {
        await updateMyStudentDocument(editingDocumentId, {
          documentType: uploadType,
          filePath: trimmedPath
        });
        setMessage("Document updated successfully.");
      }
      setEditingDocumentId(null);
      setUploadPath("");
      await loadDocuments({ keepMessage: true });
      await loadProfile({ keepMessage: true });
    } catch (err) {
      setDocumentsError(mapApiError(err));
    } finally {
      setDocumentsBusy(false);
    }
  }

  function beginDocumentEdit(document: StudentDocument): void {
    setEditingDocumentId(document.id);
    setUploadType(document.documentType);
    setUploadPath(document.filePath);
    setDocumentsError("");
    setMessage("");
  }

  async function handleDeleteDocument(documentId: number): Promise<void> {
    const confirmed = window.confirm("Delete this document?");
    if (!confirmed) return;
    setDocumentsBusy(true);
    setDocumentsError("");
    setError("");
    setMessage("");
    try {
      await deleteMyStudentDocument(documentId);
      if (editingDocumentId === documentId) {
        setEditingDocumentId(null);
        setUploadType("ID_COPY");
        setUploadPath("");
      }
      setMessage("Document deleted successfully.");
      await loadDocuments({ keepMessage: true });
      await loadProfile({ keepMessage: true });
    } catch (err) {
      setDocumentsError(mapApiError(err));
    } finally {
      setDocumentsBusy(false);
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

      {!loading && profileKind === "student" && (
        <section className="entity-form">
          <h2>Student documents</h2>
          {documentsError && <p className="error">{documentsError}</p>}
          <form className="form-grid" onSubmit={(e) => void handleSubmitDocument(e)}>
            <label>
              Document type
              <select value={uploadType} onChange={(e) => setUploadType(e.target.value as StudentDocument["documentType"])}>
                {DOCUMENT_TYPES.map((type) => (
                  <option key={type} value={type}>
                    {type}
                  </option>
                ))}
              </select>
            </label>
            <label className="full-width">
              File path
              <input
                value={uploadPath}
                onChange={(e) => setUploadPath(e.target.value)}
                placeholder="/documents/student_1/id_card.pdf"
              />
            </label>
            <div className="form-actions full-width">
              <button className="btn btn-primary" type="submit" disabled={documentsBusy}>
                {documentsBusy ? "Saving..." : editingDocumentId == null ? "Upload document" : "Save document changes"}
              </button>
              {editingDocumentId != null && (
                <button
                  className="btn btn-secondary"
                  type="button"
                  disabled={documentsBusy}
                  onClick={() => {
                    setEditingDocumentId(null);
                    setUploadType("ID_COPY");
                    setUploadPath("");
                  }}
                >
                  Cancel edit
                </button>
              )}
              <button className="btn btn-secondary" type="button" disabled={documentsBusy || documentsLoading} onClick={() => void loadDocuments()}>
                Refresh list
              </button>
            </div>
          </form>

          {documentsLoading ? (
            <p>Loading documents...</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Type</th>
                  <th>File path</th>
                  <th>Status</th>
                  <th>Uploaded</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {documents.length === 0 ? (
                  <tr>
                    <td colSpan={5}>No documents uploaded yet.</td>
                  </tr>
                ) : (
                  documents.map((document) => (
                    <tr key={document.id}>
                      <td>{document.documentType}</td>
                      <td>{document.filePath}</td>
                      <td>{document.status}</td>
                      <td>{new Date(document.uploadDate).toLocaleString()}</td>
                      <td className="actions-cell">
                        <button
                          className="btn btn-secondary btn-sm"
                          type="button"
                          disabled={documentsBusy || document.status === "APPROVED"}
                          onClick={() => beginDocumentEdit(document)}
                        >
                          Edit
                        </button>
                        <button
                          className="btn btn-danger btn-sm"
                          type="button"
                          disabled={documentsBusy || document.status === "APPROVED"}
                          onClick={() => void handleDeleteDocument(document.id)}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          )}
        </section>
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
