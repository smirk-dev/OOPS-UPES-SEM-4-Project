import Link from "next/link";
import { AppCard } from "@/components/ui/AppCard";
import { AppButton } from "@/components/ui/AppButton";

export default function HomePage() {
  return (
    <main className="mt-6 grid gap-4 md:grid-cols-2">
      <AppCard>
        <h1 className="text-2xl font-bold">UPES Campus Delivery</h1>
        <p className="mt-3 text-sm text-muted">
          Transparent MRP-first pricing, wallet-first checkout, and role-based dashboards.
        </p>
        <div className="mt-5">
          <Link href="/login">
            <AppButton>Start with Login</AppButton>
          </Link>
        </div>
      </AppCard>

      <AppCard>
        <h2 className="text-lg font-semibold">Sprint 1 Foundations</h2>
        <ul className="mt-3 space-y-2 text-sm text-muted">
          <li>Auth boundary and route protection</li>
          <li>Shared API contracts and enums</li>
          <li>Baseline schema and seed data</li>
          <li>Reusable UI primitives for later modules</li>
        </ul>
      </AppCard>
    </main>
  );
}
