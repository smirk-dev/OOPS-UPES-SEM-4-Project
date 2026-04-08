import type { InputHTMLAttributes } from "react";

type FormFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  name: string;
};

export function FormField({ label, name, ...props }: FormFieldProps) {
  return (
    <label className="block text-sm font-medium" htmlFor={name}>
      {label}
      <input
        id={name}
        name={name}
        className="mt-1 w-full rounded-md border border-[var(--card-border)] px-3 py-2"
        {...props}
      />
    </label>
  );
}
