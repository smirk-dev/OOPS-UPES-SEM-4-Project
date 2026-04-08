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
        subtitle="Role-aware landing page for the active account"
      />
      <div className="grid gap-4 md:grid-cols-3">
        <AppCard>
          <h2 className="text-lg font-semibold">Student</h2>
          <p className="mt-2 text-sm text-muted">Marketplace, cart, checkout, and wallet-first ordering.</p>
          <Link className="mt-3 inline-block text-sm font-semibold text-primary" href="/student">
            Open Student Area
          </Link>
        </AppCard>
        <AppCard>
          <h2 className="text-lg font-semibold">Vendor</h2>
          <p className="mt-2 text-sm text-muted">Product management, flash discounts, and order prep.</p>
          <Link className="mt-3 inline-block text-sm font-semibold text-primary" href="/vendor">
            Open Vendor Area
          </Link>
        </AppCard>
        <AppCard>
          <h2 className="text-lg font-semibold">Admin</h2>
          <p className="mt-2 text-sm text-muted">Governance, moderation, and audit review.</p>
          <Link className="mt-3 inline-block text-sm font-semibold text-primary" href="/admin">
            Open Admin Area
          </Link>
        </AppCard>
      </div>
      <AppCard>
        <div className="flex items-center justify-between gap-3">
          <div>
            <h3 className="text-base font-semibold">Current Session</h3>
            <p className="text-sm text-muted">You are signed in as {role || "an account"}.</p>
          </div>
          <Link href={role === Role.VENDOR ? "/vendor" : role === Role.ADMIN ? "/admin" : "/student"}>
            <AppButton variant="outline">Continue</AppButton>
          </Link>
        </div>
      </AppCard>
    </main>
  );
}
