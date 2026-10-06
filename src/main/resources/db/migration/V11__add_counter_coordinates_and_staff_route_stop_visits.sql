ALTER TABLE distributors
    ADD COLUMN latitude DOUBLE PRECISION,
    ADD COLUMN longitude DOUBLE PRECISION;

ALTER TABLE customer_counters
    ADD COLUMN address VARCHAR(500),
    ADD COLUMN latitude DOUBLE PRECISION,
    ADD COLUMN longitude DOUBLE PRECISION;

ALTER TABLE retailers
    ADD COLUMN address VARCHAR(500),
    ADD COLUMN latitude DOUBLE PRECISION,
    ADD COLUMN longitude DOUBLE PRECISION;

ALTER TABLE distributors
    ADD CONSTRAINT chk_distributors_latitude_range CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    ADD CONSTRAINT chk_distributors_longitude_range CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180));

ALTER TABLE customer_counters
    ADD CONSTRAINT chk_customer_counters_latitude_range CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    ADD CONSTRAINT chk_customer_counters_longitude_range CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180));

ALTER TABLE retailers
    ADD CONSTRAINT chk_retailers_latitude_range CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    ADD CONSTRAINT chk_retailers_longitude_range CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180));

CREATE TABLE staff_route_stop_visits (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_id                 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    route_plan_item_id       UUID NOT NULL REFERENCES route_plan_items(id) ON DELETE CASCADE,
    visit_date               DATE NOT NULL,
    check_in_time            TIMESTAMPTZ NOT NULL,
    check_out_time           TIMESTAMPTZ,
    check_in_latitude        DOUBLE PRECISION NOT NULL,
    check_in_longitude       DOUBLE PRECISION NOT NULL,
    check_in_location_name   VARCHAR(255),
    check_out_latitude       DOUBLE PRECISION,
    check_out_longitude      DOUBLE PRECISION,
    check_out_location_name  VARCHAR(255),
    check_in_distance_meters DOUBLE PRECISION,
    status                   VARCHAR(20) NOT NULL,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_stop_visit_staff_item_date UNIQUE (staff_id, route_plan_item_id, visit_date),
    CONSTRAINT chk_stop_visit_status CHECK (status IN ('PENDING', 'CHECKED_IN', 'DONE'))
);

CREATE INDEX idx_stop_visits_staff_date ON staff_route_stop_visits (staff_id, visit_date);
CREATE INDEX idx_stop_visits_item_date ON staff_route_stop_visits (route_plan_item_id, visit_date);
