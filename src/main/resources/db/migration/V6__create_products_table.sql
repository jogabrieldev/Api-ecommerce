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
    CONSTRAINT fk_product_created_by_administrator
        FOREIGN KEY (created_by_administrator_id) REFERENCES administrators (id),
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT ck_product_price CHECK (price > 0),
    CONSTRAINT ck_product_stock CHECK (stock >= 0),
    CONSTRAINT ck_product_source CHECK (source IN ('INTERNAL', 'FAKE_STORE'))
);
