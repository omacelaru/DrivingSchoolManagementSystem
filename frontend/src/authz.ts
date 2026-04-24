import { getAuthInfo, getTokenProfileId, getTokenProfileType } from "./auth";

export type AppRole = "ROLE_ADMIN" | "ROLE_INSTRUCTOR" | "ROLE_STUDENT";

export function getRoles(): AppRole[] {
  return (getAuthInfo()?.roles ?? []) as AppRole[];
}

const ROLE_LABELS: Record<AppRole, string> = {
  ROLE_ADMIN: "Administrator",
  ROLE_INSTRUCTOR: "Instructor",
  ROLE_STUDENT: "Student"
};

export function getRoleLabels(): string[] {
  return getRoles().map((role) => ROLE_LABELS[role] ?? "User");
}

export function hasAnyRole(allowedRoles: AppRole[]): boolean {
  const roles = getRoles();
  return allowedRoles.some((role) => roles.includes(role));
}

export function isAdmin(): boolean {
  return hasAnyRole(["ROLE_ADMIN"]);
}

/** Logged-in user is a student account linked to a domain student profile (not admin). */
export function isStudentScopedView(): boolean {
  const profileType = getTokenProfileType() ?? getAuthInfo()?.profileType ?? null;
  const profileId = getTokenProfileId();
  return hasAnyRole(["ROLE_STUDENT"]) && !isAdmin() && profileType === "STUDENT" && profileId != null;
}

/** Logged-in user is an instructor account linked to a domain instructor profile (not admin). */
export function isInstructorScopedView(): boolean {
  const profileType = getTokenProfileType() ?? getAuthInfo()?.profileType ?? null;
  const profileId = getTokenProfileId();
  return (
    hasAnyRole(["ROLE_INSTRUCTOR"]) && !isAdmin() && profileType === "INSTRUCTOR" && profileId != null
  );
}

export function getScopedStudentId(): number | null {
  if (!isStudentScopedView()) return null;
  return getTokenProfileId();
}

export function getScopedInstructorId(): number | null {
  if (!isInstructorScopedView()) return null;
  return getTokenProfileId();
}

export function isAuthenticated(): boolean {
  return getRoles().length > 0;
}

export function canAccessStudents(): boolean {
  return hasAnyRole(["ROLE_STUDENT", "ROLE_INSTRUCTOR", "ROLE_ADMIN"]);
}

export function canAccessInstructors(): boolean {
  return hasAnyRole(["ROLE_STUDENT", "ROLE_INSTRUCTOR", "ROLE_ADMIN"]);
}

export function canAccessVehicles(): boolean {
  return hasAnyRole(["ROLE_STUDENT", "ROLE_INSTRUCTOR", "ROLE_ADMIN"]);
}

export function canAccessCourses(): boolean {
  return hasAnyRole(["ROLE_STUDENT", "ROLE_INSTRUCTOR", "ROLE_ADMIN"]);
}

export function canAccessLessons(): boolean {
  return hasAnyRole(["ROLE_STUDENT", "ROLE_INSTRUCTOR", "ROLE_ADMIN"]);
}

export function canAccessPayments(): boolean {
  return hasAnyRole(["ROLE_STUDENT", "ROLE_ADMIN"]);
}

export function canAccessMaintenances(): boolean {
  return hasAnyRole(["ROLE_ADMIN"]);
}

export function canManageAuthAdmin(): boolean {
  return hasAnyRole(["ROLE_ADMIN"]);
}

export function canDeleteAny(): boolean {
  return hasAnyRole(["ROLE_ADMIN"]);
}

export function canRevokeOwnCourses(): boolean {
  return isInstructorScopedView();
}

export function canCreateInstructorsOrVehicles(): boolean {
  return hasAnyRole(["ROLE_ADMIN"]);
}

export function canManageCoursesOrLessons(): boolean {
  return hasAnyRole(["ROLE_ADMIN"]);
}

export function canManageLessons(): boolean {
  return hasAnyRole(["ROLE_STUDENT"]);
}

export function canCancelLessons(): boolean {
  return hasAnyRole(["ROLE_STUDENT", "ROLE_INSTRUCTOR", "ROLE_ADMIN"]);
}
