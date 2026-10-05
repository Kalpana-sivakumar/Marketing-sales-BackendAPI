-- Reconciliation migration for environments where V12 was already applied
-- before all google_place_id columns were present in the script.

ALTER TABLE customer_counters
    ADD COLUMN IF NOT EXISTS google_place_id VARCHAR(255);

ALTER TABLE distributors
    ADD COLUMN IF NOT EXISTS google_place_id VARCHAR(255);

ALTER TABLE retailers
    ADD COLUMN IF NOT EXISTS google_place_id VARCHAR(255);

ALTER TABLE staff_attendance
    ADD COLUMN IF NOT EXISTS check_in_google_place_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS check_out_google_place_id VARCHAR(255);

ALTER TABLE staff_location_tracking
    ADD COLUMN IF NOT EXISTS google_place_id VARCHAR(255);

ALTER TABLE staff_route_stop_visits
    ADD COLUMN IF NOT EXISTS check_in_google_place_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS check_out_google_place_id VARCHAR(255);
