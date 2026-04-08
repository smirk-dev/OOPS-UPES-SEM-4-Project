import { Role } from "@/lib/enums";

const TOKEN_KEY = "upes_token";
const ROLE_KEY = "upes_role";

export const authStorage = {
  set(token: string, role: Role) {
    if (typeof window === "undefined") return;
    window.localStorage.setItem(TOKEN_KEY, token);
    window.localStorage.setItem(ROLE_KEY, role);
    document.cookie = `upes_token=${token}; Path=/; Max-Age=86400; SameSite=Lax`;
    document.cookie = `upes_role=${role}; Path=/; Max-Age=86400; SameSite=Lax`;
  },

  getToken(): string {
    if (typeof window === "undefined") return "";
    return window.localStorage.getItem(TOKEN_KEY) ?? "";
  },

  clear() {
    if (typeof window === "undefined") return;
    window.localStorage.removeItem(TOKEN_KEY);
    window.localStorage.removeItem(ROLE_KEY);
    document.cookie = "upes_token=; Path=/; Max-Age=0; SameSite=Lax";
    document.cookie = "upes_role=; Path=/; Max-Age=0; SameSite=Lax";
  },

  exists(): boolean {
    if (typeof window === "undefined") return false;
    return Boolean(window.localStorage.getItem(TOKEN_KEY));
  },

  getRole(): Role | "" {
    if (typeof window === "undefined") return "";
    return (window.localStorage.getItem(ROLE_KEY) as Role | null) ?? "";
  },
};
