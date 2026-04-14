ALTER TABLE auth_users
    ADD COLUMN IF NOT EXISTS profile_type VARCHAR(30);

ALTER TABLE auth_users
    ADD COLUMN IF NOT EXISTS profile_id BIGINT;

CREATE UNIQUE INDEX IF NOT EXISTS uq_auth_users_profile_type_id
    ON auth_users(profile_type, profile_id);

