CREATE TABLE distributors (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                VARCHAR(50) NOT NULL,
    name                VARCHAR(180) NOT NULL,
    address             VARCHAR(500),
    contact_person      VARCHAR(150) NOT NULL,
    mobile              VARCHAR(20) NOT NULL,
    zone                VARCHAR(150) NOT NULL,
    route               VARCHAR(150) NOT NULL,
    assigned_staff_id   UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status              VARCHAR(20) NOT NULL,
    outstanding_amount  NUMERIC(12, 2) NOT NULL DEFAULT 0,
    last_order_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_distributors_code UNIQUE (code),
    CONSTRAINT chk_distributors_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_distributors_outstanding_non_negative CHECK (outstanding_amount >= 0)
);

CREATE TABLE customer_counters (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    distributor_id      UUID NOT NULL REFERENCES distributors(id) ON DELETE CASCADE,
    code                VARCHAR(50) NOT NULL,
    name                VARCHAR(180) NOT NULL,
    contact_person      VARCHAR(150) NOT NULL,
    mobile              VARCHAR(20) NOT NULL,
    status              VARCHAR(20) NOT NULL,
    outstanding_amount  NUMERIC(12, 2) NOT NULL DEFAULT 0,
    last_order_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_customer_counters_code UNIQUE (code),
    CONSTRAINT chk_customer_counters_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_customer_counters_outstanding_non_negative CHECK (outstanding_amount >= 0)
);

CREATE TABLE retailers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                VARCHAR(50) NOT NULL,
    name                VARCHAR(180) NOT NULL,
    contact_person      VARCHAR(150) NOT NULL,
    mobile              VARCHAR(20) NOT NULL,
    zone                VARCHAR(150) NOT NULL,
    route               VARCHAR(150) NOT NULL,
    assigned_staff_id   UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    direct_under_gen1   BOOLEAN NOT NULL DEFAULT TRUE,
    status              VARCHAR(20) NOT NULL,
    outstanding_amount  NUMERIC(12, 2) NOT NULL DEFAULT 0,
    last_order_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_retailers_code UNIQUE (code),
    CONSTRAINT chk_retailers_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_retailers_outstanding_non_negative CHECK (outstanding_amount >= 0)
);

CREATE INDEX idx_distributors_name ON distributors (name);
CREATE INDEX idx_distributors_mobile ON distributors (mobile);
CREATE INDEX idx_distributors_status ON distributors (status);
CREATE INDEX idx_distributors_assigned_staff ON distributors (assigned_staff_id);

CREATE INDEX idx_customer_counters_name ON customer_counters (name);
CREATE INDEX idx_customer_counters_mobile ON customer_counters (mobile);
CREATE INDEX idx_customer_counters_status ON customer_counters (status);
CREATE INDEX idx_customer_counters_distributor ON customer_counters (distributor_id);

CREATE INDEX idx_retailers_name ON retailers (name);
CREATE INDEX idx_retailers_mobile ON retailers (mobile);
CREATE INDEX idx_retailers_status ON retailers (status);
CREATE INDEX idx_retailers_assigned_staff ON retailers (assigned_staff_id);
