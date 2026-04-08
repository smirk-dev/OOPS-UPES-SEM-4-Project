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
    const cookieAttributes = getCookieAttributes(86400);
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
    return Boolean(readCookie(SESSION_COOKIE));
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
