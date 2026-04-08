import { Role } from "@/lib/enums";

const ROLE_KEY = "upes_role";
const TOKEN_COOKIE = "upes_token";
const ROLE_COOKIE = "upes_role";

function getCookieAttributes(maxAgeSeconds: number) {
  const secure = window.location.protocol === "https:" ? "; Secure" : "";
  return `Path=/; Max-Age=${maxAgeSeconds}; SameSite=Lax${secure}`;
}

function readCookie(name: string): string | null {
  if (typeof window === "undefined") return null;
  const encodedName = `${name}=`;
  const cookies = document.cookie.split(";");
  for (const cookie of cookies) {
    const normalized = cookie.trim();
    if (normalized.startsWith(encodedName)) {
      return decodeURIComponent(normalized.substring(encodedName.length));
    }
  }
  return null;
}

export const authStorage = {
  set(token: string, role: Role) {
    if (typeof window === "undefined") return;
    window.localStorage.setItem(ROLE_KEY, role);
    const cookieAttributes = getCookieAttributes(86400);
    document.cookie = `${TOKEN_COOKIE}=${encodeURIComponent(token)}; ${cookieAttributes}`;
    document.cookie = `${ROLE_COOKIE}=${encodeURIComponent(role)}; ${cookieAttributes}`;
  },

  getToken(): string {
    return readCookie(TOKEN_COOKIE) ?? "";
  },

  clear() {
    if (typeof window === "undefined") return;
    window.localStorage.removeItem(ROLE_KEY);
    const clearAttributes = getCookieAttributes(0);
    document.cookie = `${TOKEN_COOKIE}=; ${clearAttributes}`;
    document.cookie = `${ROLE_COOKIE}=; ${clearAttributes}`;
  },

  exists(): boolean {
    return Boolean(readCookie(TOKEN_COOKIE));
  },

  getRole(): Role | "" {
    const roleFromCookie = readCookie(ROLE_COOKIE);
    if (roleFromCookie) {
      return roleFromCookie as Role;
    }
    if (typeof window === "undefined") return "";
    return (window.localStorage.getItem(ROLE_KEY) as Role | null) ?? "";
  },
};
