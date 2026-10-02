CREATE TABLE payment_allocations (
    id UUID NOT NULL,
    payment_id UUID NOT NULL,
    administrator_id UUID NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    CONSTRAINT pk_payment_allocations PRIMARY KEY (id),
    CONSTRAINT uk_payment_allocation_administrator
        UNIQUE (payment_id, administrator_id),
    CONSTRAINT fk_allocation_payment
        FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT fk_allocation_administrator
        FOREIGN KEY (administrator_id) REFERENCES administrators (id),
    CONSTRAINT ck_payment_allocation_amount CHECK (amount > 0)
);
