import type { PropsWithChildren } from "react";

export function AppCard({ children }: PropsWithChildren) {
  return (
    <section className="rounded-xl border border-[var(--card-border)] bg-[var(--card)] p-5 shadow-card">
      {children}
    </section>
  );
}
