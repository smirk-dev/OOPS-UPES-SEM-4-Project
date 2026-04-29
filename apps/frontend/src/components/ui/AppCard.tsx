import type { PropsWithChildren } from "react";
import { clsx } from "clsx";

type AppCardProps = PropsWithChildren<{
  className?: string;
}>;

export function AppCard({ children, className }: AppCardProps) {
  return (
    <section className={clsx("neo-panel rounded-[1.25rem] border-[3px] border-[var(--card-border)] bg-[var(--card)] p-5", className)}>
      {children}
    </section>
  );
}
