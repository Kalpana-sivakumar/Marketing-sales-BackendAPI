ALTER TABLE distributors
    ADD COLUMN pinned_location_name VARCHAR(180);

ALTER TABLE customer_counters
    ADD COLUMN pinned_location_name VARCHAR(180);

ALTER TABLE retailers
    ADD COLUMN pinned_location_name VARCHAR(180);
