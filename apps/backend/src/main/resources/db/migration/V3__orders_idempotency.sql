ALTER TABLE orders
ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100);

CREATE UNIQUE INDEX IF NOT EXISTS uq_orders_student_idempotency
ON orders (student_id, idempotency_key)
WHERE idempotency_key IS NOT NULL;
