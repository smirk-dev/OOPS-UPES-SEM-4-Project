type MoneyDisplayProps = {
  amount: number;
  className?: string;
};

const moneyFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 0,
});

export function MoneyDisplay({ amount, className }: MoneyDisplayProps) {
  return <span className={className}>{moneyFormatter.format(amount)}</span>;
}
