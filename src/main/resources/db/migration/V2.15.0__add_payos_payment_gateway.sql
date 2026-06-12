ALTER TABLE deposit_orders DROP CONSTRAINT IF EXISTS deposit_orders_gateway_check;

ALTER TABLE deposit_orders
    ADD CONSTRAINT deposit_orders_gateway_check
    CHECK (gateway IN ('VNPAY', 'MOMO', 'PAYOS', 'STRIPE', 'MANUAL'));
