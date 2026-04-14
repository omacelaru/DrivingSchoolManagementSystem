CREATE TABLE IF NOT EXISTS auth_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS auth_roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS auth_user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_auth_user_roles_user FOREIGN KEY (user_id) REFERENCES auth_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_auth_user_roles_role FOREIGN KEY (role_id) REFERENCES auth_roles (id) ON DELETE CASCADE
);

INSERT INTO auth_roles(name) VALUES ('ROLE_STUDENT') ON CONFLICT (name) DO NOTHING;
INSERT INTO auth_roles(name) VALUES ('ROLE_INSTRUCTOR') ON CONFLICT (name) DO NOTHING;
INSERT INTO auth_roles(name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;

