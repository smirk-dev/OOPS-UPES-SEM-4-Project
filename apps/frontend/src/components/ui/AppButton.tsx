import { clsx } from "clsx";
import type { ButtonHTMLAttributes } from "react";

type AppButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "solid" | "outline";
};

export function AppButton({ variant = "solid", className, ...props }: AppButtonProps) {
  return (
    <button
      className={clsx(
        "rounded-md px-4 py-2 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-60",
        variant === "solid"
          ? "bg-primary text-white hover:brightness-110"
          : "border border-[var(--card-border)] bg-white text-text hover:bg-slate-50",
        className
      )}
      {...props}
    />
  );
}
