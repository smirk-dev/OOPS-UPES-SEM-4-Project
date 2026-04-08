import { MoneyDisplay } from "@/components/ui/MoneyDisplay";

type PriceRowProps = {
  label: string;
  amount: number;
  muted?: boolean;
};

export function PriceRow({ label, amount, muted = false }: PriceRowProps) {
  return (
    <div className="flex items-center justify-between text-sm">
      <span className={muted ? "text-muted" : "text-text"}>{label}</span>
      <MoneyDisplay amount={amount} className={muted ? "text-muted" : "font-semibold"} />
    </div>
  );
}
