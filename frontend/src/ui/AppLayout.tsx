import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { logout } from "../api";
import { clearAuthInfo, clearToken, getAuthInfo } from "../auth";
import { canAccessInstructors, canAccessStudents, canManageAuthAdmin } from "../authz";
import { ThemeToggle } from "./ThemeToggle";

const baseLinks = [
  { to: "/", label: "Dashboard" },
  { to: "/vehicles", label: "Vehicles" },
  { to: "/courses", label: "Courses" },
  { to: "/lessons", label: "Lessons" },
  { to: "/payments", label: "Payments" },
  { to: "/maintenances", label: "Maintenances" }
] as const;

export function AppLayout(): JSX.Element {
  const navigate = useNavigate();
  const authInfo = getAuthInfo();
  const links = [
    ...baseLinks,
    ...(canAccessStudents() ? [{ to: "/students", label: "Students" as const }] : []),
    ...(canAccessInstructors() ? [{ to: "/instructors", label: "Instructors" as const }] : []),
    ...(canManageAuthAdmin() ? [{ to: "/auth-management", label: "Auth" as const }] : [])
  ];

  async function handleLogout(): Promise<void> {
    try {
      await logout();
    } catch {
      // Stateless logout should still clear local session even if API call fails.
    }
    clearToken();
    clearAuthInfo();
    navigate("/login");
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h2 className="sidebar-brand">Driving School</h2>
        <p className="sidebar-meta">
          {authInfo?.email ?? "unknown"}
          <br />
          {(authInfo?.roles ?? []).join(", ")}
        </p>
        <nav>
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.to === "/"}
              className={({ isActive }) => (isActive ? "nav-link nav-link-active" : "nav-link")}
            >
              {link.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="sidebar-footer-row">
            <span className="sidebar-label">Theme</span>
            <ThemeToggle />
          </div>
          <button type="button" className="btn btn-secondary" onClick={() => void handleLogout()}>
            Logout
          </button>
        </div>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
