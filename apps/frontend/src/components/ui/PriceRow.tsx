import { MoneyDisplay } from "@/components/ui/MoneyDisplay";

type PriceRowProps = {
  label: string;
  amount: number;
  muted?: boolean;
};

export function PriceRow({ label, amount, muted = false }: PriceRowProps) {
  return (
    <div className="flex items-center justify-between gap-3 rounded-[0.9rem] border-[3px] border-[var(--card-border)] bg-white px-3 py-2 text-sm shadow-[4px_4px_0_var(--card-border)]">
      <span className={muted ? "font-medium text-muted" : "font-semibold text-text"}>{label}</span>
      <MoneyDisplay amount={amount} className={muted ? "font-medium text-muted" : "font-extrabold text-text"} />
    </div>
  );
}
