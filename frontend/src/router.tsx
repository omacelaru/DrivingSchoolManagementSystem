import { createBrowserRouter, Navigate } from "react-router-dom";
import { ErrorBoundary } from "./ErrorBoundary";
import { getAuthInfo, getToken, getTokenProfileId, getTokenProfileType } from "./auth";
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
import { MyProfilePage } from "./ui/MyProfilePage";

function ProtectedRoute({ children }: { children: JSX.Element }): JSX.Element {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

function RoleProtectedRoute({
  children,
  roles
}: {
  children: JSX.Element;
  roles: Array<"ROLE_ADMIN" | "ROLE_INSTRUCTOR" | "ROLE_STUDENT">;
}): JSX.Element {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  if (!hasAnyRole(roles)) {
    return <Navigate to="/" replace />;
  }
  return children;
}

/** Non-admin students must have a linked STUDENT profile to open /students. */
function StudentProfileRoute({ children }: { children: JSX.Element }): JSX.Element {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  if (!hasAnyRole(["ROLE_STUDENT", "ROLE_ADMIN"])) {
    return <Navigate to="/" replace />;
  }
  if (hasAnyRole(["ROLE_STUDENT"]) && !isAdmin()) {
    const profileType = getTokenProfileType() ?? getAuthInfo()?.profileType ?? null;
    const profileId = getTokenProfileId();
    if (profileType !== "STUDENT" || profileId == null) {
      return <Navigate to="/" replace />;
    }
  }
  return children;
}

/** Non-admin instructors must have a linked INSTRUCTOR profile to open /instructors. */
function InstructorProfileRoute({ children }: { children: JSX.Element }): JSX.Element {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  if (!hasAnyRole(["ROLE_INSTRUCTOR", "ROLE_ADMIN"])) {
    return <Navigate to="/" replace />;
  }
  if (hasAnyRole(["ROLE_INSTRUCTOR"]) && !isAdmin()) {
    const profileType = getTokenProfileType() ?? getAuthInfo()?.profileType ?? null;
    const profileId = getTokenProfileId();
    if (profileType !== "INSTRUCTOR" || profileId == null) {
      return <Navigate to="/" replace />;
    }
  }
  return children;
}

export const appRouter = createBrowserRouter([
  { path: "/login", element: <LoginPage /> },
  { path: "/register", element: <RegisterPage /> },
  {
    path: "/",
    element: (
      <ProtectedRoute>
        <ErrorBoundary>
          <AppLayout />
        </ErrorBoundary>
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <DashboardPage /> },
      { path: "my-profile", element: <MyProfilePage /> },
      {
        path: "students",
        element: (
          <StudentProfileRoute>
            <StudentsPage />
          </StudentProfileRoute>
        )
      },
      {
        path: "instructors",
        element: (
          <InstructorProfileRoute>
            <InstructorsPage />
          </InstructorProfileRoute>
        )
      },
      { path: "vehicles", element: <VehiclesPage /> },
      { path: "courses", element: <CoursesPage /> },
      { path: "lessons", element: <LessonsPage /> },
      { path: "payments", element: <PaymentsPage /> },
      { path: "maintenances", element: <MaintenancesPage /> },
      {
        path: "auth-management",
        element: (
          <RoleProtectedRoute roles={["ROLE_ADMIN"]}>
            <AuthManagementPage />
          </RoleProtectedRoute>
        )
      }
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
