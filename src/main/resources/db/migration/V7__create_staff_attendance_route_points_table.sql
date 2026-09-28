CREATE TABLE staff_attendance_route_points (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attendance_id  UUID NOT NULL REFERENCES staff_attendance(id) ON DELETE CASCADE,
    latitude       DOUBLE PRECISION NOT NULL,
    longitude      DOUBLE PRECISION NOT NULL,
    place_name     VARCHAR(255),
    recorded_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_staff_attendance_route_points_attendance_id
    ON staff_attendance_route_points (attendance_id);

CREATE INDEX idx_staff_attendance_route_points_recorded_at
    ON staff_attendance_route_points (recorded_at);
