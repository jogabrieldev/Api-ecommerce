DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM administrators administrator
        JOIN customers customer ON customer.email = administrator.email
    ) THEN
        RAISE EXCEPTION
            'Cannot migrate identities: an e-mail belongs to both a customer and an administrator';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM administrators administrator
        JOIN customers customer ON customer.cpf = administrator.cpf
    ) THEN
        RAISE EXCEPTION
            'Cannot migrate identities: a CPF belongs to both a customer and an administrator';
    END IF;
END $$;

CREATE TABLE user_identities (
    id UUID NOT NULL,
    email VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    identity_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_user_identities PRIMARY KEY (id),
    CONSTRAINT uk_user_identity_email UNIQUE (email),
    CONSTRAINT uk_user_identity_cpf UNIQUE (cpf),
    CONSTRAINT ck_user_identity_type
        CHECK (identity_type IN ('CUSTOMER', 'ADMINISTRATOR'))
);

INSERT INTO user_identities (id, email, cpf, identity_type, created_at)
SELECT gen_random_uuid(), email, cpf, 'ADMINISTRATOR', created_at
FROM administrators;

INSERT INTO user_identities (id, email, cpf, identity_type, created_at)
SELECT gen_random_uuid(), email, cpf, 'CUSTOMER', created_at
FROM customers;
