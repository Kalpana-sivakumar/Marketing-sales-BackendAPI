CREATE TABLE staff_attendance (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    check_in_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    check_in_place       VARCHAR(255) NOT NULL,
    check_out_at         TIMESTAMPTZ,
    check_out_place      VARCHAR(255)
);

CREATE INDEX idx_staff_attendance_user_id ON staff_attendance (user_id);
CREATE INDEX idx_staff_attendance_check_in_at ON staff_attendance (check_in_at DESC);
