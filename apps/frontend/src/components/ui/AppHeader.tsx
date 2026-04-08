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
    <header className="flex items-center justify-between rounded-xl border border-[var(--card-border)] bg-[var(--card)] px-4 py-3 shadow-card">
      <Link href="/" className="text-sm font-bold tracking-wide text-primary md:text-base">
        UPES Campus Delivery
      </Link>
      <div className="flex flex-wrap items-center gap-2">
        {isLoggedIn && pathname !== "/login" ? (
          <>
            <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-muted">
              {role || "SIGNED-IN"}
            </span>
            {navigation.filter((item) => item.show).map((item) => (
              <Link key={item.href} href={item.href} className="text-sm font-semibold text-primary">
                {item.label}
              </Link>
            ))}
            <AppButton variant="outline" onClick={onLogout}>
              Logout
            </AppButton>
          </>
        ) : (
          <Link href="/login" className="text-sm font-semibold text-primary">
            Login
          </Link>
        )}
      </div>
    </header>
  );
}

function isExamModeActive() {
  const hour = new Date().getHours();
  return hour >= 22 || hour < 6;
}
