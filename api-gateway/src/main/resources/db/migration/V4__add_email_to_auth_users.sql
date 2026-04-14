ALTER TABLE auth_users
    ADD COLUMN IF NOT EXISTS email VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uq_auth_users_email ON auth_users(email);

