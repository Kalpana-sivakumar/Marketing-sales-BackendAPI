CREATE TABLE routes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(180) NOT NULL,
    zone        VARCHAR(150) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_routes_name_zone UNIQUE (name, zone)
);

CREATE INDEX idx_routes_name ON routes (name);
CREATE INDEX idx_routes_zone ON routes (zone);
CREATE INDEX idx_routes_active ON routes (active);

ALTER TABLE distributors
    ADD COLUMN master_route_id UUID REFERENCES routes(id) ON DELETE SET NULL;

ALTER TABLE customer_counters
    ADD COLUMN master_route_id UUID REFERENCES routes(id) ON DELETE SET NULL;

ALTER TABLE retailers
    ADD COLUMN master_route_id UUID REFERENCES routes(id) ON DELETE SET NULL;

CREATE INDEX idx_distributors_master_route ON distributors (master_route_id);
CREATE INDEX idx_customer_counters_master_route ON customer_counters (master_route_id);
CREATE INDEX idx_retailers_master_route ON retailers (master_route_id);

CREATE TABLE route_plans (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_id      UUID NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    week_start    DATE NOT NULL,
    staff_id      UUID REFERENCES users(id) ON DELETE RESTRICT,
    status        VARCHAR(20) NOT NULL,
    published_at  TIMESTAMPTZ,
    version       BIGINT NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_route_plans_route_week UNIQUE (route_id, week_start),
    CONSTRAINT chk_route_plans_status CHECK (status IN ('DRAFT', 'PUBLISHED'))
);

CREATE INDEX idx_route_plans_week_start ON route_plans (week_start);
CREATE INDEX idx_route_plans_staff_week ON route_plans (staff_id, week_start);

CREATE TABLE route_plan_items (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_plan_id  UUID NOT NULL REFERENCES route_plans(id) ON DELETE CASCADE,
    week_start     DATE NOT NULL,
    counter_type   VARCHAR(20) NOT NULL,
    counter_id     UUID NOT NULL,
    visit_order    INTEGER NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_route_plan_items_plan_order UNIQUE (route_plan_id, visit_order),
    CONSTRAINT uk_route_plan_items_week_counter UNIQUE (week_start, counter_type, counter_id),
    CONSTRAINT chk_route_plan_items_counter_type CHECK (counter_type IN ('DISTRIBUTOR', 'CUSTOMER', 'RETAILER')),
    CONSTRAINT chk_route_plan_items_visit_order CHECK (visit_order > 0)
);

CREATE INDEX idx_route_plan_items_plan ON route_plan_items (route_plan_id);
CREATE INDEX idx_route_plan_items_week_counter ON route_plan_items (week_start, counter_type, counter_id);
