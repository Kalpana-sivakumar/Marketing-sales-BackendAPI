CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    sku             VARCHAR(50) NOT NULL,
    product_name    VARCHAR(200) NOT NULL,
    category        VARCHAR(120) NOT NULL,
    pack_size       INTEGER NOT NULL,
    uom             VARCHAR(20) NOT NULL,
    base_price      NUMERIC(12, 2) NOT NULL,
    mrp             NUMERIC(12, 2) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    created_by      VARCHAR(150) NOT NULL,
    updated_by      VARCHAR(150) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT chk_products_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_products_pack_size_positive CHECK (pack_size > 0),
    CONSTRAINT chk_products_base_price_positive CHECK (base_price > 0),
    CONSTRAINT chk_products_mrp_positive CHECK (mrp > 0),
    CONSTRAINT chk_products_mrp_vs_base CHECK (mrp >= base_price)
);

CREATE INDEX idx_products_name ON products (product_name);
CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_uom ON products (uom);
CREATE INDEX idx_products_status ON products (status);
