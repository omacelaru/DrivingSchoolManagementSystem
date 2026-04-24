import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { logout } from "../api";
import { clearAuthInfo, clearToken, getAuthInfo } from "../auth";
import {
  canAccessCourses,
  canAccessInstructors,
  canAccessLessons,
  canAccessMaintenances,
  canAccessPayments,
  canAccessStudents,
  canAccessVehicles,
  canManageAuthAdmin,
  getRoleLabels
} from "../authz";
import { ThemeToggle } from "./ThemeToggle";

export function AppLayout(): JSX.Element {
  const navigate = useNavigate();
  const authInfo = getAuthInfo();
  const roleLabels = getRoleLabels();
  const links = [
    { to: "/", label: "Dashboard" as const },
    ...(canAccessVehicles() ? [{ to: "/vehicles", label: "Vehicles" as const }] : []),
    ...(canAccessCourses() ? [{ to: "/courses", label: "Courses" as const }] : []),
    ...(canAccessLessons() ? [{ to: "/lessons", label: "Lessons" as const }] : []),
    ...(canAccessPayments() ? [{ to: "/payments", label: "Payments" as const }] : []),
    ...(canAccessMaintenances() ? [{ to: "/maintenances", label: "Maintenances" as const }] : []),
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
        <div className="sidebar-identity">
          <p className="sidebar-meta">
            <NavLink to="/my-profile" className="sidebar-email-link">
              <span className="sidebar-email">@{authInfo?.email ?? "unknown"}</span>
            </NavLink>
            <span className="sidebar-role-badge">
              <span className="sidebar-role-value">{(roleLabels ?? []).join(", ") || "User"}</span>
            </span>
          </p>
        </div>
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
        <div className="content-inner">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
