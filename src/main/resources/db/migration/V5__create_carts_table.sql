CREATE TABLE carts (
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_carts PRIMARY KEY (id),
    CONSTRAINT fk_cart_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT ck_cart_status
        CHECK (status IN ('ACTIVE', 'COMPLETED', 'ABANDONED'))
);
