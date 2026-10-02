CREATE INDEX ix_carts_customer_status
    ON carts (customer_id, status);

CREATE INDEX ix_products_category_active
    ON products (category_id, active);

CREATE INDEX ix_products_administrator
    ON products (created_by_administrator_id);

CREATE INDEX ix_cart_items_product
    ON cart_items (product_id);

CREATE INDEX ix_orders_customer_created_at
    ON orders (customer_id, created_at DESC);

CREATE INDEX ix_order_items_product
    ON order_items (product_id);

CREATE INDEX ix_payments_customer_status
    ON payments (customer_id, status);

CREATE INDEX ix_payments_cart
    ON payments (cart_id);

CREATE INDEX ix_payment_allocations_administrator
    ON payment_allocations (administrator_id);
