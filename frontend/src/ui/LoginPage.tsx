import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../api";
import { getToken, setAuthInfo, setToken } from "../auth";
import { ThemeToggle } from "./ThemeToggle";

export function LoginPage(): JSX.Element {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (getToken()) {
      navigate("/", { replace: true });
    }
  }, [navigate]);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      const response = await login(email, password);
      setToken(response.accessToken);
      setAuthInfo({
        email: response.username,
        roles: response.roles,
        profileType: response.profileType
      });
      navigate("/", { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page-split">
      <div className="auth-theme-corner">
        <ThemeToggle />
      </div>
      
      {/* Left side: branding/logo/hero */}
      <div className="auth-split-left">
        <div className="auth-branding-container">
          <div className="auth-logo">
            <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2" />
              <circle cx="7" cy="17" r="2" />
              <path d="M9 17h6" />
              <circle cx="17" cy="17" r="2" />
            </svg>
          </div>
          <h1 className="auth-title">Macelaru Academy</h1>
          <p className="auth-subtitle">Învață să conduci cu încredere.</p>
        </div>
        
        <div className="auth-footer-credit">
          © {new Date().getFullYear()} Macelaru Academy. Toate drepturile rezervate.
        </div>
      </div>
      
      {/* Right side: Login form */}
      <div className="auth-split-right">
        <div className="auth-right-container">
          {/* Logo visible only on mobile/small screens */}
          <div className="auth-mobile-logo-header">
            <div className="auth-logo">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2" />
                <circle cx="7" cy="17" r="2" />
                <path d="M9 17h6" />
                <circle cx="17" cy="17" r="2" />
              </svg>
            </div>
            <h2>Macelaru Academy</h2>
          </div>
          
          <form className="card" onSubmit={handleSubmit}>
            <h1>Autentificare</h1>
            <label>
              Email
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="nume@exemplu.ro" required />
            </label>
            <label>
              Parolă
              <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" required />
            </label>
            {error && <p className="error">{error}</p>}
            <button className="btn btn-primary" disabled={loading} type="submit">
              {loading ? "Se autentifică..." : "Intră în cont"}
            </button>
            <p>
              Nu ai cont încă? <Link to="/register">Creează unul</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
}

