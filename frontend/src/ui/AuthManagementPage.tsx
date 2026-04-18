import { useState } from "react";
import { ApiError, registerAdmin, registerInstructor, registerStudent } from "../api";

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
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const [studentEmail, setStudentEmail] = useState("");
  const [studentPassword, setStudentPassword] = useState("");
  const [studentFirstName, setStudentFirstName] = useState("");
  const [studentLastName, setStudentLastName] = useState("");
  const [studentCnp, setStudentCnp] = useState("");
  const [studentPhone, setStudentPhone] = useState("");
  const [studentAddress, setStudentAddress] = useState("");
  const [studentCategories, setStudentCategories] = useState("B");

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
          targetDrivingCategoryCodes: studentCategories
            .split(",")
            .map((code) => code.trim().toUpperCase())
            .filter((code) => code.length > 0)
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

      <form className="entity-form" onSubmit={handleRegisterStudent}>
        <h2>Register Student</h2>
        <div className="form-grid">
          <label>Email<input type="email" value={studentEmail} onChange={(e) => setStudentEmail(e.target.value)} required /></label>
          <label>Password<input type="password" value={studentPassword} onChange={(e) => setStudentPassword(e.target.value)} required /></label>
          <label>First name<input value={studentFirstName} onChange={(e) => setStudentFirstName(e.target.value)} required /></label>
          <label>Last name<input value={studentLastName} onChange={(e) => setStudentLastName(e.target.value)} required /></label>
          <label>CNP<input value={studentCnp} onChange={(e) => setStudentCnp(e.target.value)} required /></label>
          <label>Phone<input value={studentPhone} onChange={(e) => setStudentPhone(e.target.value)} required /></label>
          <label className="full-width">Address<input value={studentAddress} onChange={(e) => setStudentAddress(e.target.value)} required /></label>
          <label className="full-width">Target categories<input value={studentCategories} onChange={(e) => setStudentCategories(e.target.value)} required /></label>
        </div>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary">
            Register student account
          </button>
        </div>
      </form>

      <form className="entity-form" onSubmit={handleRegisterInstructor}>
        <h2>Register Instructor</h2>
        <div className="form-grid">
          <label>Email<input type="email" value={instructorEmail} onChange={(e) => setInstructorEmail(e.target.value)} required /></label>
          <label>Password<input type="password" value={instructorPassword} onChange={(e) => setInstructorPassword(e.target.value)} required /></label>
          <label>First name<input value={instructorFirstName} onChange={(e) => setInstructorFirstName(e.target.value)} required /></label>
          <label>Last name<input value={instructorLastName} onChange={(e) => setInstructorLastName(e.target.value)} required /></label>
          <label>License number<input value={instructorLicense} onChange={(e) => setInstructorLicense(e.target.value)} required /></label>
          <label>Phone<input value={instructorPhone} onChange={(e) => setInstructorPhone(e.target.value)} required /></label>
          <label>Specialization
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

      <form className="entity-form" onSubmit={handleRegisterAdmin}>
        <h2>Register Admin</h2>
        <div className="form-grid">
          <label>Email<input type="email" value={adminEmail} onChange={(e) => setAdminEmail(e.target.value)} required /></label>
          <label>Password<input type="password" value={adminPassword} onChange={(e) => setAdminPassword(e.target.value)} required /></label>
        </div>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary">
            Register admin account
          </button>
        </div>
      </form>
    </section>
  );
}
