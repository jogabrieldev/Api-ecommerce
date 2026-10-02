CREATE INDEX IF NOT EXISTS ix_carts_customer_status ON carts (customer_id, status);
CREATE INDEX IF NOT EXISTS ix_products_category_active ON products (category_id, active);
CREATE INDEX IF NOT EXISTS ix_products_administrator ON products (created_by_administrator_id);
CREATE INDEX IF NOT EXISTS ix_payments_customer_status ON payments (customer_id, status);
CREATE INDEX IF NOT EXISTS ix_payments_cart ON payments (cart_id);
CREATE INDEX IF NOT EXISTS ix_orders_customer_created_at ON orders (customer_id, created_at DESC);
