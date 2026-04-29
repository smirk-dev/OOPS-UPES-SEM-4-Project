import Link from "next/link";
import { AppCard } from "@/components/ui/AppCard";
import { AppButton } from "@/components/ui/AppButton";

export default function HomePage() {
  return (
    <main className="mt-6 space-y-4">
      <AppCard className="overflow-hidden bg-[var(--card)]">
        <div className="grid gap-6 lg:grid-cols-[1.4fr_0.9fr] lg:items-center">
          <div className="space-y-4">
            <span className="inline-flex rounded-full border-[3px] border-[var(--card-border)] bg-[var(--surface-alt)] px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.16em] shadow-[4px_4px_0_var(--card-border)]">
              Campus storefront
            </span>
            <h1 className="max-w-2xl text-4xl font-black tracking-tight sm:text-5xl lg:text-6xl">
              Delivery that feels loud, fast, and built for campus life.
            </h1>
            <p className="max-w-xl text-sm font-medium text-muted sm:text-base">
              Transparent pricing, wallet-first checkout, and role-aware dashboards wrapped in a bold neo-brutalist shell.
            </p>
            <div className="flex flex-wrap gap-3">
              <Link href="/login">
                <AppButton>Start with Login</AppButton>
              </Link>
              <Link href="/signup">
                <AppButton variant="outline">Create Account</AppButton>
              </Link>
            </div>
          </div>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-1">
            <div className="neo-floating rounded-[1.25rem] border-[3px] border-[var(--card-border)] bg-[var(--surface-alt-2)] p-4">
              <div className="text-xs font-black uppercase tracking-[0.18em]">Wallet first</div>
              <div className="mt-2 text-3xl font-black">INR checkout</div>
              <p className="mt-2 text-sm font-medium">Discounts, savings, and totals stay visible at every step.</p>
            </div>
            <div className="neo-floating rounded-[1.25rem] border-[3px] border-[var(--card-border)] bg-[var(--surface-alt)] p-4">
              <div className="text-xs font-black uppercase tracking-[0.18em]">Role aware</div>
              <div className="mt-2 text-3xl font-black">Student / Vendor / Admin</div>
              <p className="mt-2 text-sm font-medium">Each workspace gets its own visual treatment and controls.</p>
            </div>
          </div>
        </div>
      </AppCard>

      <div className="grid gap-4 md:grid-cols-3">
        <AppCard className="bg-[var(--surface-alt)]">
          <h2 className="text-lg font-black uppercase tracking-[0.1em]">Loud UI</h2>
          <p className="mt-3 text-sm font-medium text-muted">
            Thick outlines, offset shadows, and bright panels replace the default muted SaaS look.
          </p>
        </AppCard>
        <AppCard className="bg-[var(--surface-alt-2)]">
          <h2 className="text-lg font-black uppercase tracking-[0.1em]">Fast flow</h2>
          <p className="mt-3 text-sm font-medium text-muted">
            Signup, login, shopping, checkout, and moderation sit in one consistent visual language.
          </p>
        </AppCard>
        <AppCard className="bg-[#d7f4c1]">
          <h2 className="text-lg font-black uppercase tracking-[0.1em]">Campus-ready</h2>
          <p className="mt-3 text-sm font-medium text-muted">
            The interface is built to feel energetic on the landing page and practical in the workspace.
          </p>
        </AppCard>
      </div>
    </main>
  );
}
