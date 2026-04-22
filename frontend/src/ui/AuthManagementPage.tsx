import { useMemo, useState } from "react";
import { ApiError, registerAdmin, registerInstructor, registerStudent } from "../api";
import {
  DRIVING_LICENSE_CATEGORY_CODES,
  expandDrivingCategories,
  LICENSE_CATEGORY_HINTS,
  type DrivingLicenseCategoryCode
} from "../constants/drivingLicenseCategories";

function mapApiError(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return error instanceof Error ? error.message : "Unexpected error";
  }
  if (error.status === 409) return "Conflict: account already exists.";
  if (error.status === 400) return `Validation failed: ${error.message}`;
  if (error.status === 403) return "Forbidden: only admin can call this endpoint.";
  return error.message;
}

export function AuthManagementPage(): JSX.Element {
  const [activeTab, setActiveTab] = useState<"student" | "instructor" | "admin">("student");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const [studentEmail, setStudentEmail] = useState("");
  const [studentPassword, setStudentPassword] = useState("");
  const [studentFirstName, setStudentFirstName] = useState("");
  const [studentLastName, setStudentLastName] = useState("");
  const [studentCnp, setStudentCnp] = useState("");
  const [studentPhone, setStudentPhone] = useState("");
  const [studentAddress, setStudentAddress] = useState("");
  const [selectedStudentCategories, setSelectedStudentCategories] = useState<Set<DrivingLicenseCategoryCode>>(() => new Set(["B"]));

  const [instructorEmail, setInstructorEmail] = useState("");
  const [instructorPassword, setInstructorPassword] = useState("");
  const [instructorFirstName, setInstructorFirstName] = useState("");
  const [instructorLastName, setInstructorLastName] = useState("");
  const [instructorLicense, setInstructorLicense] = useState("");
  const [instructorPhone, setInstructorPhone] = useState("");
  const [instructorSpecialization, setInstructorSpecialization] = useState<"THEORETICAL" | "PRACTICAL" | "BOTH">(
    "BOTH"
  );

  const [adminEmail, setAdminEmail] = useState("");
  const [adminPassword, setAdminPassword] = useState("");
  const expandedStudentCategories = useMemo(
    () => expandDrivingCategories(selectedStudentCategories),
    [selectedStudentCategories]
  );

  function toggleInSet(set: Set<DrivingLicenseCategoryCode>, code: DrivingLicenseCategoryCode): Set<DrivingLicenseCategoryCode> {
    const next = new Set(set);
    if (next.has(code)) {
      next.delete(code);
    } else {
      next.add(code);
    }
    return next;
  }

  async function handleRegisterStudent(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    setMessage("");
    setError("");
    try {
      const response = await registerStudent({
        email: studentEmail.trim(),
        password: studentPassword,
        studentProfile: {
          firstName: studentFirstName.trim(),
          lastName: studentLastName.trim(),
          cnp: studentCnp.trim(),
          phone: studentPhone.trim(),
          address: studentAddress.trim(),
          targetDrivingCategoryCodes: DRIVING_LICENSE_CATEGORY_CODES.filter((code) => expandedStudentCategories.has(code))
        }
      });
      setMessage(`Student account created: userId=${response.userId}, role=${response.role}`);
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  async function handleRegisterInstructor(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    setMessage("");
    setError("");
    try {
      const response = await registerInstructor({
        email: instructorEmail.trim(),
        password: instructorPassword,
        instructorProfile: {
          firstName: instructorFirstName.trim(),
          lastName: instructorLastName.trim(),
          licenseNumber: instructorLicense.trim().toUpperCase(),
          phone: instructorPhone.trim(),
          specialization: instructorSpecialization
        }
      });
      setMessage(`Instructor account created: userId=${response.userId}, role=${response.role}`);
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  async function handleRegisterAdmin(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    setMessage("");
    setError("");
    try {
      const response = await registerAdmin({
        email: adminEmail.trim(),
        password: adminPassword
      });
      setMessage(`Admin account created: userId=${response.userId}, role=${response.role}`);
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  return (
    <section className="page">
      <h1>Auth Management</h1>
      <p className="dashboard-lead">Covers all API Gateway auth endpoints: register student/instructor/admin.</p>
      {message && <p className="message-success">{message}</p>}
      {error && <p className="error">{error}</p>}

      <div className="entity-form">
        <h2>Register account type</h2>
        <div className="form-actions">
          <button type="button" className={activeTab === "student" ? "btn btn-primary" : "btn btn-secondary"} onClick={() => setActiveTab("student")}>
            Student
          </button>
          <button type="button" className={activeTab === "instructor" ? "btn btn-primary" : "btn btn-secondary"} onClick={() => setActiveTab("instructor")}>
            Instructor
          </button>
          <button type="button" className={activeTab === "admin" ? "btn btn-primary" : "btn btn-secondary"} onClick={() => setActiveTab("admin")}>
            Admin
          </button>
        </div>
      </div>

      {activeTab === "student" && (
        <form className="entity-form" onSubmit={handleRegisterStudent}>
          <h2>Register Student</h2>
          <div className="form-grid">
            <label>Email<input type="email" value={studentEmail} onChange={(e) => setStudentEmail(e.target.value)} required /></label>
            <label>Password<input type="password" value={studentPassword} onChange={(e) => setStudentPassword(e.target.value)} required minLength={6} /></label>
            <label>First name<input value={studentFirstName} onChange={(e) => setStudentFirstName(e.target.value)} required /></label>
            <label>Last name<input value={studentLastName} onChange={(e) => setStudentLastName(e.target.value)} required /></label>
            <label>CNP<input value={studentCnp} onChange={(e) => setStudentCnp(e.target.value.replace(/\D/g, "").slice(0, 13))} required maxLength={13} /></label>
            <label>Phone<input value={studentPhone} onChange={(e) => setStudentPhone(e.target.value.replace(/\D/g, "").slice(0, 10))} required maxLength={10} /></label>
            <label className="full-width">Address<input value={studentAddress} onChange={(e) => setStudentAddress(e.target.value)} required /></label>
            <fieldset className="full-width category-fieldset">
              <legend className="register-label">Target driving licence categories</legend>
              <div className="category-grid">
                {DRIVING_LICENSE_CATEGORY_CODES.map((code) => {
                  const hint = LICENSE_CATEGORY_HINTS[code];
                  const explicitlySelected = selectedStudentCategories.has(code);
                  const autoIncluded = expandedStudentCategories.has(code) && !explicitlySelected;
                  return (
                    <label key={code} className="category-card">
                      <input
                        type="checkbox"
                        checked={expandedStudentCategories.has(code)}
                        disabled={autoIncluded}
                        onChange={() => setSelectedStudentCategories((prev) => toggleInSet(prev, code))}
                      />
                      <span className="category-card-body">
                        <span className="category-code">{code}</span>
                        <span className="category-hint">{hint}</span>
                      </span>
                    </label>
                  );
                })}
              </div>
            </fieldset>
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              Register student account
            </button>
          </div>
        </form>
      )}

      {activeTab === "instructor" && (
        <form className="entity-form" onSubmit={handleRegisterInstructor}>
          <h2>Register Instructor</h2>
          <div className="form-grid">
            <label>Email<input type="email" value={instructorEmail} onChange={(e) => setInstructorEmail(e.target.value)} required /></label>
            <label>Password<input type="password" value={instructorPassword} onChange={(e) => setInstructorPassword(e.target.value)} required minLength={6} /></label>
            <label>First name<input value={instructorFirstName} onChange={(e) => setInstructorFirstName(e.target.value)} required /></label>
            <label>Last name<input value={instructorLastName} onChange={(e) => setInstructorLastName(e.target.value)} required /></label>
            <label>License number<input value={instructorLicense} onChange={(e) => setInstructorLicense(e.target.value)} required /></label>
            <label>Phone<input value={instructorPhone} onChange={(e) => setInstructorPhone(e.target.value.replace(/\D/g, "").slice(0, 10))} required maxLength={10} /></label>
            <label>
              Specialization
              <select
                value={instructorSpecialization}
                onChange={(e) => setInstructorSpecialization(e.target.value as "THEORETICAL" | "PRACTICAL" | "BOTH")}
              >
                <option value="THEORETICAL">THEORETICAL</option>
                <option value="PRACTICAL">PRACTICAL</option>
                <option value="BOTH">BOTH</option>
              </select>
            </label>
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              Register instructor account
            </button>
          </div>
        </form>
      )}

      {activeTab === "admin" && (
        <form className="entity-form" onSubmit={handleRegisterAdmin}>
          <h2>Register Admin</h2>
          <div className="form-grid">
            <label>Email<input type="email" value={adminEmail} onChange={(e) => setAdminEmail(e.target.value)} required /></label>
            <label>Password<input type="password" value={adminPassword} onChange={(e) => setAdminPassword(e.target.value)} required minLength={6} /></label>
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              Register admin account
            </button>
          </div>
        </form>
      )}
    </section>
  );
}
