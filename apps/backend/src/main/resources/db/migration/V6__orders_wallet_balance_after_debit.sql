ALTER TABLE orders
ADD COLUMN IF NOT EXISTS wallet_balance_after_debit NUMERIC(12,2);

UPDATE orders
SET wallet_balance_after_debit = COALESCE(wallet_balance_after_debit, 0)
WHERE wallet_balance_after_debit IS NULL;

ALTER TABLE orders
ALTER COLUMN wallet_balance_after_debit SET DEFAULT 0,
ALTER COLUMN wallet_balance_after_debit SET NOT NULL;
