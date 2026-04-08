import { Role } from "@/lib/enums";

const ROLE_KEY = "upes_role";
const SESSION_COOKIE = "upes_session";

function getCookieAttributes(maxAgeSeconds: number) {
  const secure = window.location.protocol === "https:" ? "; Secure" : "";
  return `Path=/; Max-Age=${maxAgeSeconds}; SameSite=Lax${secure}`;
}

export const authStorage = {
  set(token: string, role: Role) {
    if (typeof window === "undefined") return;
    window.localStorage.setItem(ROLE_KEY, role);
    const cookieAttributes = getCookieAttributes(getSessionMaxAgeSeconds(token));
    document.cookie = `${SESSION_COOKIE}=${encodeURIComponent(token)}; ${cookieAttributes}`;
  },

  getToken(): string {
    if (typeof window === "undefined") return "";
    return readCookie(SESSION_COOKIE);
  },

  clear() {
    if (typeof window === "undefined") return;
    window.localStorage.removeItem(ROLE_KEY);
    const clearAttributes = getCookieAttributes(0);
    document.cookie = `${SESSION_COOKIE}=; ${clearAttributes}`;
  },

  exists(): boolean {
    if (typeof window === "undefined") return false;
    const token = readCookie(SESSION_COOKIE);
    return token.length > 0 && !isJwtExpired(token);
  },

  getRole(): Role | "" {
    if (typeof window === "undefined") return "";
    return (window.localStorage.getItem(ROLE_KEY) as Role | null) ?? "";
  },
};

function readCookie(name: string): string {
  const encodedName = `${encodeURIComponent(name)}=`;
  const cookies = document.cookie.split(";");
  for (const cookie of cookies) {
    const value = cookie.trim();
    if (value.startsWith(encodedName)) {
      return decodeURIComponent(value.slice(encodedName.length));
    }
  }
  return "";
}

function getSessionMaxAgeSeconds(token: string): number {
  const exp = getJwtExp(token);
  if (exp == null) {
    return 86400;
  }

  const nowSeconds = Math.floor(Date.now() / 1000);
  const ttl = exp - nowSeconds;
  return ttl > 0 ? ttl : 0;
}

function isJwtExpired(token: string): boolean {
  const exp = getJwtExp(token);
  if (exp == null) {
    return false;
  }
  return Math.floor(Date.now() / 1000) >= exp;
}

function getJwtExp(token: string): number | null {
  const parts = token.split(".");
  if (parts.length !== 3) {
    return null;
  }

  try {
    const payloadBase64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = payloadBase64.padEnd(Math.ceil(payloadBase64.length / 4) * 4, "=");
    const payload = JSON.parse(atob(padded)) as { exp?: unknown };
    return typeof payload.exp === "number" ? payload.exp : null;
  } catch {
    return null;
  }
}
