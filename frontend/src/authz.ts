import { getAuthInfo } from "./auth";

export type AppRole = "ROLE_ADMIN" | "ROLE_INSTRUCTOR" | "ROLE_STUDENT";

export function getRoles(): AppRole[] {
  return (getAuthInfo()?.roles ?? []) as AppRole[];
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
  const info = getAuthInfo();
  return hasAnyRole(["ROLE_STUDENT"]) && !isAdmin() && info?.profileType === "STUDENT" && info.profileId != null;
}

/** Logged-in user is an instructor account linked to a domain instructor profile (not admin). */
export function isInstructorScopedView(): boolean {
  const info = getAuthInfo();
  return (
    hasAnyRole(["ROLE_INSTRUCTOR"]) && !isAdmin() && info?.profileType === "INSTRUCTOR" && info.profileId != null
  );
}

export function getScopedStudentId(): number | null {
  if (!isStudentScopedView()) return null;
  return getAuthInfo()?.profileId ?? null;
}

export function getScopedInstructorId(): number | null {
  if (!isInstructorScopedView()) return null;
  return getAuthInfo()?.profileId ?? null;
}

export function isAuthenticated(): boolean {
  return getRoles().length > 0;
}

export function canAccessStudents(): boolean {
  return hasAnyRole(["ROLE_STUDENT", "ROLE_ADMIN"]);
}

export function canAccessInstructors(): boolean {
  return hasAnyRole(["ROLE_INSTRUCTOR", "ROLE_ADMIN"]);
}

export function canManageAuthAdmin(): boolean {
  return hasAnyRole(["ROLE_ADMIN"]);
}

export function canDeleteAny(): boolean {
  return hasAnyRole(["ROLE_ADMIN"]);
}

export function canCreateInstructorsOrVehicles(): boolean {
  return hasAnyRole(["ROLE_INSTRUCTOR", "ROLE_ADMIN"]);
}
