import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ApiError, registerInstructor, registerStudent } from "../api";
import {
  DRIVING_LICENSE_CATEGORY_CODES,
  expandDrivingCategories,
  LICENSE_CATEGORY_HINTS,
  type DrivingLicenseCategoryCode
} from "../constants/drivingLicenseCategories";
import { ThemeToggle } from "./ThemeToggle";

type RegisterType = "student" | "instructor";

function mapApiError(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return error instanceof Error ? error.message : "Unexpected error";
  }
  if (error.status === 409) return "Account already exists for this email.";
  if (error.status === 400) return `Validation failed: ${error.message}`;
  return error.message;
}

function toggleInSet(set: Set<DrivingLicenseCategoryCode>, code: DrivingLicenseCategoryCode): Set<DrivingLicenseCategoryCode> {
  const next = new Set(set);
  if (next.has(code)) {
    next.delete(code);
  } else {
    next.add(code);
  }
  return next;
}

export function RegisterPage(): JSX.Element {
  const navigate = useNavigate();
  const [registerType, setRegisterType] = useState<RegisterType>("student");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");

  const [studentCnp, setStudentCnp] = useState("");
  const [studentAddress, setStudentAddress] = useState("");
  const [selectedCategories, setSelectedCategories] = useState<Set<DrivingLicenseCategoryCode>>(() => new Set(["B"]));

  const [phone, setPhone] = useState("");
  const [instructorLicense, setInstructorLicense] = useState("");
  const [instructorSpecialization, setInstructorSpecialization] = useState<"THEORETICAL" | "PRACTICAL" | "BOTH">(
    "BOTH"
  );

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const expandedCategories = useMemo(() => expandDrivingCategories(selectedCategories), [selectedCategories]);

  const categorySelectionInvalid = useMemo(
    () => registerType === "student" && expandedCategories.size === 0,
    [registerType, expandedCategories]
  );

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    setLoading(true);
    setError("");
    setMessage("");
    try {
      if (registerType === "student") {
        const targetDrivingCategoryCodes = DRIVING_LICENSE_CATEGORY_CODES.filter((c) => expandedCategories.has(c));
        if (targetDrivingCategoryCodes.length === 0) {
          setError("Select at least one target driving licence category.");
          setLoading(false);
          return;
        }
        await registerStudent({
          email: email.trim(),
          password,
          studentProfile: {
            firstName: firstName.trim(),
            lastName: lastName.trim(),
            cnp: studentCnp.trim(),
            phone: phone.trim(),
            address: studentAddress.trim(),
            targetDrivingCategoryCodes
          }
        });
      } else {
        await registerInstructor({
          email: email.trim(),
          password,
          instructorProfile: {
            firstName: firstName.trim(),
            lastName: lastName.trim(),
            licenseNumber: instructorLicense.trim().toUpperCase(),
            phone: phone.trim(),
            specialization: instructorSpecialization
          }
        });
      }
      setMessage("Account created successfully. You can now sign in.");
      setTimeout(() => navigate("/login"), 800);
    } catch (err) {
      setError(mapApiError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-theme-corner">
        <ThemeToggle />
      </div>
      <div className="centered-page">
        <form className="card register-card register-card--wide" onSubmit={handleSubmit} noValidate>
          <header className="register-header">
            <h1>Create account</h1>
            <p className="register-lead">
              Choose your role, then fill in the sections below.
            </p>
          </header>

          <div className="register-type-toggle" role="group" aria-label="Account type">
            <button
              type="button"
              className={registerType === "student" ? "register-type-btn is-active" : "register-type-btn"}
              onClick={() => setRegisterType("student")}
              aria-pressed={registerType === "student"}
            >
              <span className="register-type-title">Student</span>
              <span className="register-type-desc">Enrol for lessons &amp; exams</span>
            </button>
            <button
              type="button"
              className={registerType === "instructor" ? "register-type-btn is-active" : "register-type-btn"}
              onClick={() => setRegisterType("instructor")}
              aria-pressed={registerType === "instructor"}
            >
              <span className="register-type-title">Instructor</span>
              <span className="register-type-desc">Teach theory or practical sessions</span>
            </button>
          </div>

          <section className="register-section" aria-labelledby="sec-account">
            <h2 id="sec-account" className="register-section-title">
              Account
            </h2>
            <div className="register-grid">
              <label className="register-field">
                <span className="register-label">Email</span>
                <input
                  type="email"
                  autoComplete="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="you@example.com"
                  required
                />
              </label>
              <label className="register-field">
                <span className="register-label">Password</span>
                <input
                  type="password"
                  autoComplete="new-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="At least 6 characters"
                  required
                  minLength={6}
                />
              </label>
            </div>
          </section>

          <section className="register-section" aria-labelledby="sec-profile">
            <h2 id="sec-profile" className="register-section-title">
              Profile
            </h2>
            <div className="register-grid">
              <label className="register-field">
                <span className="register-label">First name</span>
                <input
                  autoComplete="given-name"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  placeholder="e.g. Maria"
                  required
                />
              </label>
              <label className="register-field">
                <span className="register-label">Last name</span>
                <input
                  autoComplete="family-name"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  placeholder="e.g. Popescu"
                  required
                />
              </label>
              <label className="register-field register-field--full">
                <span className="register-label">Phone</span>
                <input
                  type="tel"
                  inputMode="numeric"
                  autoComplete="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value.replace(/\D/g, "").slice(0, 10))}
                  placeholder="10 digits, e.g. 0722123456"
                  required
                  maxLength={10}
                />
              </label>
            </div>
          </section>

          {registerType === "student" ? (
            <>
              <section className="register-section" aria-labelledby="sec-student-id">
                <h2 id="sec-student-id" className="register-section-title">
                  Identity &amp; address
                </h2>
                <p className="register-section-desc">Required for student registration in Romania.</p>
                <div className="register-grid">
                  <label className="register-field">
                    <span className="register-label">CNP (personal numeric code)</span>
                    <input
                      inputMode="numeric"
                      value={studentCnp}
                      onChange={(e) => setStudentCnp(e.target.value.replace(/\D/g, ""))}
                      placeholder="13 digits"
                      required
                      maxLength={13}
                      minLength={13}
                      pattern="\d{13}"
                    />
                  </label>
                  <label className="register-field register-field--full">
                    <span className="register-label">Home address</span>
                    <input
                      autoComplete="street-address"
                      value={studentAddress}
                      onChange={(e) => setStudentAddress(e.target.value)}
                      placeholder="Street, number, city, county"
                      required
                    />
                  </label>
                </div>
              </section>

              <section className="register-section" aria-labelledby="sec-categories">
                <h2 id="sec-categories" className="register-section-title">
                  Target driving licence categories
                </h2>
                <p className="register-section-desc">
                  Select all categories you want to train for — you can pick more than one.
                </p>
                <fieldset className="category-fieldset">
                  <legend className="sr-only">Select categories</legend>
                  <div className="category-grid">
                    {DRIVING_LICENSE_CATEGORY_CODES.map((code) => {
                      const hint = LICENSE_CATEGORY_HINTS[code as DrivingLicenseCategoryCode];
                      const id = `cat-${code}`;
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
                </fieldset>
                {categorySelectionInvalid && (
                  <p className="error register-inline-error" role="alert">
                    Select at least one category.
                  </p>
                )}
              </section>
            </>
          ) : (
            <section className="register-section" aria-labelledby="sec-instructor">
              <h2 id="sec-instructor" className="register-section-title">
                Instructor details
              </h2>
              <label className="register-field register-field--block">
                <span className="register-label">Instructor licence number</span>
                <input
                  value={instructorLicense}
                  onChange={(e) => setInstructorLicense(e.target.value)}
                  placeholder="e.g. LIC-12345"
                  required
                  autoCapitalize="characters"
                />
              </label>

              <div className="register-subsection">
                <span className="register-label register-label--block">Specialization</span>
                <p className="field-hint register-subsection-lead">What type of lessons are you qualified to deliver?</p>
                <div className="specialization-row" role="radiogroup" aria-label="Specialization">
                  {(
                    [
                      { value: "THEORETICAL" as const, title: "Theory", desc: "Classroom / online theory" },
                      { value: "PRACTICAL" as const, title: "Practical", desc: "In-vehicle training" },
                      { value: "BOTH" as const, title: "Both", desc: "Theory and practical" }
                    ] as const
                  ).map((opt) => (
                    <label
                      key={opt.value}
                      className={
                        instructorSpecialization === opt.value ? "specialization-card is-selected" : "specialization-card"
                      }
                    >
                      <input
                        type="radio"
                        name="specialization"
                        value={opt.value}
                        checked={instructorSpecialization === opt.value}
                        onChange={() => setInstructorSpecialization(opt.value)}
                      />
                      <span className="specialization-title">{opt.title}</span>
                      <span className="specialization-desc">{opt.desc}</span>
                    </label>
                  ))}
                </div>
              </div>
            </section>
          )}

          {error && (
            <p className="error" role="alert">
              {error}
            </p>
          )}
          {message && <p className="message-success">{message}</p>}

          <div className="register-actions">
            <button className="btn btn-primary" disabled={loading || categorySelectionInvalid} type="submit">
              {loading ? "Creating account..." : "Create account"}
            </button>
          </div>
          <p className="register-footer-link">
            Already have an account? <Link to="/login">Sign in</Link>
          </p>
        </form>
      </div>
    </div>
  );
}
