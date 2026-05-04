import type { InputHTMLAttributes } from "react";

type FormFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  name: string;
};

export function FormField({ label, name, ...props }: FormFieldProps) {
  return (
    <label className="block text-sm font-semibold text-text" htmlFor={name}>
      <span className="mb-1 inline-flex rounded-full border-[3px] border-[var(--card-border)] bg-[var(--surface-alt)] px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.14em] shadow-[4px_4px_0_var(--card-border)]">
        {label}
      </span>
      <input id={name} name={name} className="mt-2 w-full px-4 py-3" {...props} />
    </label>
  );
}
