type LoadingStateProps = {
  label?: string;
};

export function LoadingState({ label = "Loading..." }: LoadingStateProps) {
  return (
    <div className="rounded-md border border-dashed border-[var(--card-border)] p-4 text-sm text-muted">
      {label}
    </div>
  );
}
