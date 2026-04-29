import { clsx } from "clsx";
import type { ButtonHTMLAttributes } from "react";

type AppButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "solid" | "outline";
};

export function AppButton({ variant = "solid", className, ...props }: AppButtonProps) {
  return (
    <button
      className={clsx(
        "neo-pill px-4 py-2 text-sm font-extrabold uppercase tracking-[0.08em] transition duration-200 disabled:cursor-not-allowed disabled:opacity-60",
        variant === "solid"
          ? "bg-primary text-white hover:-translate-x-0.5 hover:-translate-y-0.5"
          : "bg-[var(--card)] text-[var(--text)] hover:-translate-x-0.5 hover:-translate-y-0.5",
        className
      )}
      {...props}
    />
  );
}
