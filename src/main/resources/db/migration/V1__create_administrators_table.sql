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
