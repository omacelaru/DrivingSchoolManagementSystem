const TOKEN_KEY = "drivingSchool.token";
const AUTH_INFO_KEY = "drivingSchool.authInfo";

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export type AuthInfo = {
  email: string;
  roles: string[];
  profileType: string | null;
  profileId: number | null;
};

export function setAuthInfo(authInfo: AuthInfo): void {
  localStorage.setItem(AUTH_INFO_KEY, JSON.stringify(authInfo));
}

export function getAuthInfo(): AuthInfo | null {
  const raw = localStorage.getItem(AUTH_INFO_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as AuthInfo;
  } catch {
    return null;
  }
}

export function clearAuthInfo(): void {
  localStorage.removeItem(AUTH_INFO_KEY);
}
