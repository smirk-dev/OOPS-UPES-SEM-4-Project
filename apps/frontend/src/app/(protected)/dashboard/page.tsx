"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { AppCard } from "@/components/ui/AppCard";
import { PageHeader } from "@/components/ui/PageHeader";
import { AppButton } from "@/components/ui/AppButton";
import { authStorage } from "@/lib/auth-storage";
import { Role } from "@/lib/enums";

export default function DashboardPage() {
  const [role, setRole] = useState<Role | "">("");

  useEffect(() => {
    setRole(authStorage.getRole());
  }, []);

  return (
    <main className="mt-6 space-y-4">
      <PageHeader
        title="Authenticated Dashboard"
        subtitle="A loud, role-aware control room for every account type in the app"
      />
      <div className="grid gap-4 md:grid-cols-3">
        <AppCard className="bg-[var(--surface-alt)]">
          <h2 className="text-lg font-black uppercase tracking-[0.08em]">Student</h2>
          <p className="mt-2 text-sm font-medium text-muted">Marketplace, cart, checkout, and wallet-first ordering.</p>
          <Link className="mt-3 inline-flex rounded-full border-[3px] border-[var(--card-border)] bg-[var(--card)] px-3 py-1 text-xs font-extrabold uppercase tracking-[0.1em] shadow-[4px_4px_0_var(--card-border)]" href="/student">
            Open Student Area
          </Link>
        </AppCard>
        <AppCard className="bg-[var(--surface-alt-2)]">
          <h2 className="text-lg font-black uppercase tracking-[0.08em]">Vendor</h2>
          <p className="mt-2 text-sm font-medium text-muted">Product management, flash discounts, and order prep.</p>
          <Link className="mt-3 inline-flex rounded-full border-[3px] border-[var(--card-border)] bg-[var(--card)] px-3 py-1 text-xs font-extrabold uppercase tracking-[0.1em] shadow-[4px_4px_0_var(--card-border)]" href="/vendor">
            Open Vendor Area
          </Link>
        </AppCard>
        <AppCard className="bg-[#d7f4c1]">
          <h2 className="text-lg font-black uppercase tracking-[0.08em]">Admin</h2>
          <p className="mt-2 text-sm font-medium text-muted">Governance, moderation, and audit review.</p>
          <Link className="mt-3 inline-flex rounded-full border-[3px] border-[var(--card-border)] bg-[var(--card)] px-3 py-1 text-xs font-extrabold uppercase tracking-[0.1em] shadow-[4px_4px_0_var(--card-border)]" href="/admin">
            Open Admin Area
          </Link>
        </AppCard>
      </div>
      <AppCard className="bg-[var(--card)]">
        <div className="flex items-center justify-between gap-3">
          <div>
            <h3 className="text-base font-black uppercase tracking-[0.1em]">Current Session</h3>
            <p className="text-sm font-medium text-muted">You are signed in as {role || "an account"}.</p>
          </div>
          <Link href={role === Role.VENDOR ? "/vendor" : role === Role.ADMIN ? "/admin" : "/student"}>
            <AppButton variant="outline">Continue</AppButton>
          </Link>
        </div>
      </AppCard>
    </main>
  );
}
