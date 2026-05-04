type LoadingStateProps = {
  label?: string;
};

export function LoadingState({ label = "Loading..." }: LoadingStateProps) {
  return (
    <div className="neo-panel flex items-center gap-3 rounded-[1rem] border-[3px] border-[var(--card-border)] bg-[var(--surface-alt)] px-4 py-3 text-sm font-semibold text-text">
      <span className="h-3 w-3 animate-pulse rounded-full bg-[var(--accent)]" />
      <span>{label}</span>
    </div>
  );
}
