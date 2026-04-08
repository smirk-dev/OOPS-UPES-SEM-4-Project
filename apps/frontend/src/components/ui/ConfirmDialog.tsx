"use client";

import { AppButton } from "@/components/ui/AppButton";

type ConfirmDialogProps = {
  title: string;
  description: string;
  onConfirm: () => void;
  onCancel: () => void;
};

export function ConfirmDialog({ title, description, onConfirm, onCancel }: ConfirmDialogProps) {
  return (
    <div className="rounded-xl border border-[var(--card-border)] bg-[var(--card)] p-4 text-[var(--text)] shadow-card">
      <h3 className="text-base font-semibold text-[var(--text)]">{title}</h3>
      <p className="mt-2 text-sm text-muted">{description}</p>
      <div className="mt-4 flex gap-2">
        <AppButton variant="outline" onClick={onCancel}>
          Cancel
        </AppButton>
        <AppButton onClick={onConfirm}>Confirm</AppButton>
      </div>
    </div>
  );
}
