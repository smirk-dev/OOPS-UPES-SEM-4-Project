type PageHeaderProps = {
  title: string;
  subtitle?: string;
};

export function PageHeader({ title, subtitle }: PageHeaderProps) {
  return (
    <header className="space-y-2">
      <div className="inline-flex rounded-full border-[3px] border-[var(--card-border)] bg-[var(--surface-alt)] px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.16em] shadow-[4px_4px_0_var(--card-border)]">
        Campus delivery
      </div>
      <h1 className="max-w-3xl text-4xl font-black tracking-tight sm:text-5xl">{title}</h1>
      {subtitle ? <p className="max-w-2xl text-sm font-medium text-muted sm:text-base">{subtitle}</p> : null}
    </header>
  );
}
