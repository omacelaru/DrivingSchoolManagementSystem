import { createBrowserRouter, Navigate } from "react-router-dom";
import { ErrorBoundary } from "./ErrorBoundary";
import { getAuthInfo, getToken } from "./auth";
import { hasAnyRole, isAdmin } from "./authz";
import { AppLayout } from "./ui/AppLayout";
import { RegisterPage } from "./ui/RegisterPage";
import { DashboardPage } from "./ui/DashboardPage";
import { InstructorsPage } from "./ui/InstructorsPage";
import { LessonsPage } from "./ui/LessonsPage";
import { LoginPage } from "./ui/LoginPage";
import { MaintenancesPage } from "./ui/MaintenancesPage";
import { PaymentsPage } from "./ui/PaymentsPage";
import { StudentsPage } from "./ui/StudentsPage";
import { VehiclesPage } from "./ui/VehiclesPage";
import { CoursesPage } from "./ui/CoursesPage";
import { AuthManagementPage } from "./ui/AuthManagementPage";

function protectedElement(element: JSX.Element): JSX.Element {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  return element;
}

function roleProtectedElement(element: JSX.Element, roles: Array<"ROLE_ADMIN" | "ROLE_INSTRUCTOR" | "ROLE_STUDENT">): JSX.Element {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  if (!hasAnyRole(roles)) {
    return <Navigate to="/" replace />;
  }
  return element;
}

/** Non-admin students must have a linked STUDENT profile to open /students. */
function studentProfileGuard(element: JSX.Element): JSX.Element {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  if (!hasAnyRole(["ROLE_STUDENT", "ROLE_ADMIN"])) {
    return <Navigate to="/" replace />;
  }
  if (hasAnyRole(["ROLE_STUDENT"]) && !isAdmin()) {
    const info = getAuthInfo();
    if (info?.profileType !== "STUDENT" || info.profileId == null) {
      return <Navigate to="/" replace />;
    }
  }
  return element;
}

/** Non-admin instructors must have a linked INSTRUCTOR profile to open /instructors. */
function instructorProfileGuard(element: JSX.Element): JSX.Element {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  if (!hasAnyRole(["ROLE_INSTRUCTOR", "ROLE_ADMIN"])) {
    return <Navigate to="/" replace />;
  }
  if (hasAnyRole(["ROLE_INSTRUCTOR"]) && !isAdmin()) {
    const info = getAuthInfo();
    if (info?.profileType !== "INSTRUCTOR" || info.profileId == null) {
      return <Navigate to="/" replace />;
    }
  }
  return element;
}

export const appRouter = createBrowserRouter([
  { path: "/login", element: <LoginPage /> },
  { path: "/register", element: <RegisterPage /> },
  {
    path: "/",
    element: protectedElement(
      <ErrorBoundary>
        <AppLayout />
      </ErrorBoundary>
    ),
    children: [
      { index: true, element: <DashboardPage /> },
      { path: "students", element: studentProfileGuard(<StudentsPage />) },
      { path: "instructors", element: instructorProfileGuard(<InstructorsPage />) },
      { path: "vehicles", element: <VehiclesPage /> },
      { path: "courses", element: <CoursesPage /> },
      { path: "lessons", element: <LessonsPage /> },
      { path: "payments", element: <PaymentsPage /> },
      { path: "maintenances", element: <MaintenancesPage /> },
      { path: "auth-management", element: roleProtectedElement(<AuthManagementPage />, ["ROLE_ADMIN"]) }
    ]
  },
  {
    path: "*",
    element: (
      <div className="auth-page">
        <div className="centered-page">
          <div className="card page-not-found">
            <h1>404 — Page not found</h1>
            <p>The page you requested does not exist.</p>
          </div>
        </div>
      </div>
    )
  }
]);
