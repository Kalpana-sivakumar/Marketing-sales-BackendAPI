-- Ensures gen_random_uuid() is available even on Postgres < 13
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name           VARCHAR(150) NOT NULL,
    email               VARCHAR(180) NOT NULL,
    password            VARCHAR(255) NOT NULL,
    phone               VARCHAR(20),
    role                VARCHAR(30)  NOT NULL,
    enabled             BOOLEAN      NOT NULL DEFAULT TRUE,
    account_non_locked  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'MARKETING_MANAGER', 'STAFF'))
);

CREATE INDEX idx_users_role ON users (role);
