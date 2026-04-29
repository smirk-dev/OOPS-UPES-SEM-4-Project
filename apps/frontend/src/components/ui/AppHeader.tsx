"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { AppButton } from "@/components/ui/AppButton";
import { Role } from "@/lib/enums";
import { authStorage } from "@/lib/auth-storage";

export function AppHeader() {
  const pathname = usePathname();
  const router = useRouter();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [role, setRole] = useState<Role | "">("");

  useEffect(() => {
    const sync = () => {
      setIsLoggedIn(authStorage.exists());
      setRole(authStorage.getRole());
      document.body.classList.toggle("exam-mode", isExamModeActive());
    };

    const interval = window.setInterval(sync, 60_000);
    const onStorage = () => sync();
    window.addEventListener("storage", onStorage);
    sync();

    return () => {
      window.clearInterval(interval);
      window.removeEventListener("storage", onStorage);
    };
  }, [pathname]);

  const onLogout = () => {
    authStorage.clear();
    router.push("/login");
    router.refresh();
  };

  const navigation = [
    { href: "/dashboard", label: "Dashboard", show: true },
    { href: "/student", label: "Student", show: role === Role.STUDENT || role === "" },
    { href: "/vendor", label: "Vendor", show: role === Role.VENDOR || role === Role.ADMIN },
    { href: "/admin", label: "Admin", show: role === Role.ADMIN },
  ];

  return (
    <header className="neo-panel flex flex-wrap items-center justify-between gap-3 rounded-[1.25rem] border-[3px] border-[var(--card-border)] bg-[var(--card)] px-4 py-3">
      <Link href="/" className="inline-flex items-center gap-2 text-sm font-black uppercase tracking-[0.12em] text-primary md:text-base">
        <span className="inline-flex h-8 w-8 items-center justify-center rounded-full border-[3px] border-[var(--card-border)] bg-[var(--surface-alt-2)] shadow-[4px_4px_0_var(--card-border)]">
          CD
        </span>
        UPES Campus Delivery
      </Link>
      <div className="flex flex-wrap items-center gap-2">
        {isLoggedIn && pathname !== "/login" ? (
          <>
            <span className="neo-pill px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.12em] text-text">
              {role || "SIGNED-IN"}
            </span>
            {navigation.filter((item) => item.show).map((item) => (
              <Link key={item.href} href={item.href} className="neo-pill px-3 py-1 text-xs font-extrabold uppercase tracking-[0.1em] text-primary transition hover:-translate-x-0.5 hover:-translate-y-0.5">
                {item.label}
              </Link>
            ))}
            <AppButton variant="outline" onClick={onLogout}>
              Logout
            </AppButton>
          </>
        ) : (
          <div className="flex flex-wrap items-center gap-2">
            <Link href="/signup" className="neo-pill px-3 py-1 text-xs font-extrabold uppercase tracking-[0.1em] text-primary transition hover:-translate-x-0.5 hover:-translate-y-0.5">
              Signup
            </Link>
            <Link href="/login" className="neo-pill px-3 py-1 text-xs font-extrabold uppercase tracking-[0.1em] text-primary transition hover:-translate-x-0.5 hover:-translate-y-0.5">
              Login
            </Link>
          </div>
        )}
      </div>
    </header>
  );
}

function isExamModeActive() {
  const hour = new Date().getHours();
  return hour >= 22 || hour < 6;
}
