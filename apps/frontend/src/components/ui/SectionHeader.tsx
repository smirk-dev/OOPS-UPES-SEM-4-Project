import type { ReactNode } from "react";

type SectionHeaderProps = {
  title: string;
  action?: ReactNode;
};

export function SectionHeader({ title, action }: SectionHeaderProps) {
  return (
    <div className="flex items-center justify-between">
      <h2 className="inline-flex items-center gap-2 rounded-full border-[3px] border-[var(--card-border)] bg-[var(--surface-alt-2)] px-3 py-1 text-sm font-extrabold uppercase tracking-[0.12em] shadow-[4px_4px_0_var(--card-border)]">
        {title}
      </h2>
      {action}
    </div>
  );
}
