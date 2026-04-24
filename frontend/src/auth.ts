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
};

export function setAuthInfo(authInfo: AuthInfo): void {
  // Persist only non-sensitive UI context.
  localStorage.setItem(
    AUTH_INFO_KEY,
    JSON.stringify({
      email: authInfo.email,
      roles: authInfo.roles,
      profileType: authInfo.profileType
    } satisfies AuthInfo)
  );
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

type JwtPayload = {
  profileId?: number | string | null;
  profileType?: string | null;
};

function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const parts = token.split(".");
    if (parts.length < 2) return null;
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64 + "=".repeat((4 - (base64.length % 4)) % 4);
    const json = atob(padded);
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}

export function getTokenProfileId(): number | null {
  const token = getToken();
  if (!token) return null;
  const payload = decodeJwtPayload(token);
  const raw = payload?.profileId;
  if (typeof raw === "number" && Number.isFinite(raw)) return raw;
  if (typeof raw === "string" && raw.trim() !== "" && !Number.isNaN(Number(raw))) return Number(raw);
  return null;
}

export function getTokenProfileType(): string | null {
  const token = getToken();
  if (!token) return null;
  const payload = decodeJwtPayload(token);
  return typeof payload?.profileType === "string" ? payload.profileType : null;
}
