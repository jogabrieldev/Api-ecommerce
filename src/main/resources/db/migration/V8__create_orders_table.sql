CREATE TABLE orders (
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    cart_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    total NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT uk_order_cart UNIQUE (cart_id),
    CONSTRAINT fk_order_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_order_cart
        FOREIGN KEY (cart_id) REFERENCES carts (id),
    CONSTRAINT ck_order_status CHECK (status IN ('COMPLETED')),
    CONSTRAINT ck_order_total CHECK (total > 0)
);
