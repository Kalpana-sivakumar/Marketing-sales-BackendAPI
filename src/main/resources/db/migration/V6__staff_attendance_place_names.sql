-- Replace raw GPS coordinates with human-readable place names
-- (resolved on the mobile device and sent with check-in/check-out).
ALTER TABLE staff_attendance
    ADD COLUMN check_in_place  VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN check_out_place VARCHAR(255),
    DROP COLUMN check_in_latitude,
    DROP COLUMN check_in_longitude,
    DROP COLUMN check_out_latitude,
    DROP COLUMN check_out_longitude;

ALTER TABLE staff_attendance ALTER COLUMN check_in_place DROP DEFAULT;
