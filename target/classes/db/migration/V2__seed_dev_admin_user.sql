-- Dev/demo convenience seed only.
-- Email: admin@marketingsales.dev | Password: Admin@12345
-- Do NOT rely on this in staging/production — create real users via /api/auth/register instead,
-- and remove or guard this migration per-environment if needed.
INSERT INTO users (full_name, email, password, role, enabled, account_non_locked)
VALUES (
    'System Admin',
    'admin@marketingsales.dev',
    '$2b$10$v9OfZQpJ5xmpQypPpNp0r.t1MnSAs4pX6OjDgmSu3gsr3tfr0yazC',
    'ADMIN',
    TRUE,
    TRUE
)
ON CONFLICT (email) DO NOTHING;
