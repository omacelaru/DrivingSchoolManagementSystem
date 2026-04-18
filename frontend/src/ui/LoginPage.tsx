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
    <div className="auth-page">
      <div className="auth-theme-corner">
        <ThemeToggle />
      </div>
      <div className="centered-page">
      <form className="card" onSubmit={handleSubmit}>
        <h1>Login</h1>
        <label>
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label>
          Password
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>
        {error && <p className="error">{error}</p>}
        <button className="btn btn-primary" disabled={loading} type="submit">
          {loading ? "Signing in..." : "Sign in"}
        </button>
        <p>
          No account yet? <Link to="/register">Create one</Link>
        </p>
      </form>
      </div>
    </div>
  );
}
