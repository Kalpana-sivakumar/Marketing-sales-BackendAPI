ALTER TABLE staff_attendance
    ADD COLUMN check_in_google_place_id VARCHAR(255),
    ADD COLUMN check_out_google_place_id VARCHAR(255);

ALTER TABLE staff_location_tracking
    ADD COLUMN google_place_id VARCHAR(255);

ALTER TABLE staff_route_stop_visits
    ADD COLUMN check_in_google_place_id VARCHAR(255),
    ADD COLUMN check_out_google_place_id VARCHAR(255);

ALTER TABLE distributors
    ADD COLUMN google_place_id VARCHAR(255);

ALTER TABLE customer_counters
    ADD COLUMN google_place_id VARCHAR(255);

ALTER TABLE retailers
    ADD COLUMN google_place_id VARCHAR(255);
