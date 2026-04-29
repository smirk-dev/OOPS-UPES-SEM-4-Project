import { clsx } from "clsx";

type StatusBadgeProps = {
  text: string;
  tone?: "success" | "warn" | "neutral";
};

export function StatusBadge({ text, tone = "neutral" }: StatusBadgeProps) {
  return (
    <span
      className={clsx(
        "inline-flex rounded-full border-[3px] border-[var(--card-border)] px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.12em] shadow-[4px_4px_0_var(--card-border)]",
        tone === "success" && "bg-[#b7f3c7] text-[#111111]",
        tone === "warn" && "bg-[#ffe08a] text-[#111111]",
        tone === "neutral" && "bg-[var(--surface-alt)] text-[#111111]"
      )}
    >
      {text}
    </span>
  );
}
