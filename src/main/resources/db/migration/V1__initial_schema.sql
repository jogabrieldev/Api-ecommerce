CREATE TABLE administrators (
    id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_administrators PRIMARY KEY (id),
    CONSTRAINT uk_administrator_email UNIQUE (email),
    CONSTRAINT uk_administrator_cpf UNIQUE (cpf),
    CONSTRAINT ck_administrator_role CHECK (role IN ('ADMIN', 'MANAGER'))
);

CREATE TABLE categories (
    id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uk_category_name UNIQUE (name)
);

CREATE TABLE customers (
    id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    phone VARCHAR(11) NOT NULL,
    birth_date DATE NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uk_customer_email UNIQUE (email),
    CONSTRAINT uk_customer_cpf UNIQUE (cpf)
);

CREATE TABLE customer_addresses (
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    zip_code VARCHAR(8) NOT NULL,
    street VARCHAR(150) NOT NULL,
    number VARCHAR(20) NOT NULL,
    complement VARCHAR(100),
    neighborhood VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    CONSTRAINT pk_customer_addresses PRIMARY KEY (id),
    CONSTRAINT uk_customer_address_customer UNIQUE (customer_id),
    CONSTRAINT fk_customer_address_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE carts (
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_carts PRIMARY KEY (id),
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT ck_cart_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'ABANDONED'))
);

CREATE TABLE products (
    id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    price NUMERIC(10,2) NOT NULL,
    stock INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    external_id VARCHAR(100),
    source VARCHAR(30) NOT NULL,
    image_url VARCHAR(1000),
    created_by_administrator_id UUID NOT NULL,
    category_id UUID NOT NULL,
    version BIGINT,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uk_product_source_external_id UNIQUE (source, external_id),
    CONSTRAINT fk_product_created_by_administrator FOREIGN KEY (created_by_administrator_id) REFERENCES administrators (id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT ck_product_price CHECK (price > 0),
    CONSTRAINT ck_product_stock CHECK (stock >= 0),
    CONSTRAINT ck_product_source CHECK (source IN ('INTERNAL', 'FAKE_STORE'))
);

CREATE TABLE cart_items (
    id UUID NOT NULL,
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL,
    CONSTRAINT pk_cart_items PRIMARY KEY (id),
    CONSTRAINT uk_cart_item_product UNIQUE (cart_id, product_id),
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES carts (id),
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_cart_item_quantity CHECK (quantity > 0)
);

CREATE TABLE orders (
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    cart_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    total NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT uk_order_cart UNIQUE (cart_id),
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_order_cart FOREIGN KEY (cart_id) REFERENCES carts (id),
    CONSTRAINT ck_order_status CHECK (status IN ('COMPLETED'))
);

CREATE TABLE order_items (
    id UUID NOT NULL,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT uk_order_item_product UNIQUE (order_id, product_id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_order_item_quantity CHECK (quantity > 0)
);

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
    CONSTRAINT fk_payment_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_payment_cart FOREIGN KEY (cart_id) REFERENCES carts (id),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT ck_payment_method CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'PIX')),
    CONSTRAINT ck_payment_status CHECK (status IN ('PROCESSING', 'APPROVED', 'DECLINED'))
);

CREATE TABLE payment_allocations (
    id UUID NOT NULL,
    payment_id UUID NOT NULL,
    administrator_id UUID NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    CONSTRAINT pk_payment_allocations PRIMARY KEY (id),
    CONSTRAINT uk_payment_allocation_administrator UNIQUE (payment_id, administrator_id),
    CONSTRAINT fk_allocation_payment FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT fk_allocation_administrator FOREIGN KEY (administrator_id) REFERENCES administrators (id)
);
