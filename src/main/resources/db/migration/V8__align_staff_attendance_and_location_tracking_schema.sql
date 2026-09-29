ALTER TABLE staff_attendance
    RENAME COLUMN user_id TO staff_id;

ALTER TABLE staff_attendance
    RENAME COLUMN check_in_at TO check_in_time;

ALTER TABLE staff_attendance
    RENAME COLUMN check_out_at TO check_out_time;

ALTER TABLE staff_attendance
    RENAME COLUMN check_in_place TO check_in_location_name;

ALTER TABLE staff_attendance
    RENAME COLUMN check_out_place TO check_out_location_name;

ALTER TABLE staff_attendance
    ADD COLUMN attendance_date DATE,
    ADD COLUMN check_in_latitude DOUBLE PRECISION,
    ADD COLUMN check_in_longitude DOUBLE PRECISION,
    ADD COLUMN check_out_latitude DOUBLE PRECISION,
    ADD COLUMN check_out_longitude DOUBLE PRECISION,
    ADD COLUMN status VARCHAR(30),
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

UPDATE staff_attendance
SET attendance_date = check_in_time::date,
    status = CASE
                 WHEN check_out_time IS NULL THEN 'CHECKED_IN'
                 ELSE 'CHECKED_OUT'
             END;

ALTER TABLE staff_attendance
    ALTER COLUMN attendance_date SET NOT NULL,
    ALTER COLUMN status SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_staff_attendance_staff_id ON staff_attendance (staff_id);
CREATE INDEX IF NOT EXISTS idx_staff_attendance_check_in_time ON staff_attendance (check_in_time DESC);
CREATE INDEX IF NOT EXISTS idx_staff_attendance_attendance_date ON staff_attendance (attendance_date DESC);

ALTER TABLE staff_attendance_route_points
    RENAME TO staff_location_tracking;

ALTER TABLE staff_location_tracking
    RENAME COLUMN place_name TO location_name;

ALTER TABLE staff_location_tracking
    ADD COLUMN staff_id UUID;

UPDATE staff_location_tracking slt
SET staff_id = sa.staff_id
FROM staff_attendance sa
WHERE sa.id = slt.attendance_id;

ALTER TABLE staff_location_tracking
    ALTER COLUMN staff_id SET NOT NULL;

ALTER TABLE staff_location_tracking
    ADD CONSTRAINT fk_staff_location_tracking_staff
        FOREIGN KEY (staff_id) REFERENCES users (id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_staff_location_tracking_attendance_id ON staff_location_tracking (attendance_id);
CREATE INDEX IF NOT EXISTS idx_staff_location_tracking_staff_id ON staff_location_tracking (staff_id);
CREATE INDEX IF NOT EXISTS idx_staff_location_tracking_recorded_at ON staff_location_tracking (recorded_at);
