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

-- password for all seed users is: password
INSERT INTO auth_users(username, password_hash, enabled)
VALUES ('student', '$2a$10$7EqJtq98hPqEX7fNZaFWoOq9P4v7YQqL5jL7lqXWfM9vDOMkMt2Zu', TRUE)
ON CONFLICT (username) DO NOTHING;

INSERT INTO auth_users(username, password_hash, enabled)
VALUES ('instructor', '$2a$10$7EqJtq98hPqEX7fNZaFWoOq9P4v7YQqL5jL7lqXWfM9vDOMkMt2Zu', TRUE)
ON CONFLICT (username) DO NOTHING;

INSERT INTO auth_users(username, password_hash, enabled)
VALUES ('admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoOq9P4v7YQqL5jL7lqXWfM9vDOMkMt2Zu', TRUE)
ON CONFLICT (username) DO NOTHING;

INSERT INTO auth_user_roles(user_id, role_id)
SELECT u.id, r.id
FROM auth_users u
JOIN auth_roles r ON r.name = 'ROLE_STUDENT'
WHERE u.username = 'student'
ON CONFLICT DO NOTHING;

INSERT INTO auth_user_roles(user_id, role_id)
SELECT u.id, r.id
FROM auth_users u
JOIN auth_roles r ON r.name = 'ROLE_INSTRUCTOR'
WHERE u.username = 'instructor'
ON CONFLICT DO NOTHING;

INSERT INTO auth_user_roles(user_id, role_id)
SELECT u.id, r.id
FROM auth_users u
JOIN auth_roles r ON r.name = 'ROLE_ADMIN'
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;

