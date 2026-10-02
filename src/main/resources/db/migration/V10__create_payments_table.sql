CREATE TABLE payments (
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    cart_id UUID NOT NULL,
    order_id UUID,
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(36) NOT NULL,
    gateway_reference VARCHAR(80),
    decline_reason VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT uk_payment_order UNIQUE (order_id),
    CONSTRAINT uk_payment_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_payment_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_payment_cart
        FOREIGN KEY (cart_id) REFERENCES carts (id),
    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT ck_payment_amount CHECK (amount > 0),
    CONSTRAINT ck_payment_currency CHECK (currency = 'BRL'),
    CONSTRAINT ck_payment_method
        CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'PIX')),
    CONSTRAINT ck_payment_status
        CHECK (status IN ('PROCESSING', 'APPROVED', 'DECLINED'))
);
