ALTER TABLE users
    ADD COLUMN region VARCHAR(150) NOT NULL DEFAULT 'All regions',
    ADD COLUMN last_login_at TIMESTAMPTZ;

ALTER TABLE users
    ALTER COLUMN region DROP DEFAULT;

CREATE INDEX idx_users_region ON users (region);
CREATE INDEX idx_users_enabled ON users (enabled);
