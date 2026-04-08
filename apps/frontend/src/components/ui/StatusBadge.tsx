import { clsx } from "clsx";

type StatusBadgeProps = {
  text: string;
  tone?: "success" | "warn" | "neutral";
};

export function StatusBadge({ text, tone = "neutral" }: StatusBadgeProps) {
  return (
    <span
      className={clsx(
        "inline-flex rounded-full px-2.5 py-1 text-xs font-semibold",
        tone === "success" && "bg-emerald-100 text-emerald-700",
        tone === "warn" && "bg-amber-100 text-amber-700",
        tone === "neutral" && "bg-slate-100 text-slate-700"
      )}
    >
      {text}
    </span>
  );
}
