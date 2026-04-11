-- Instructor service

CREATE TABLE instructors (
    id               BIGSERIAL PRIMARY KEY,
    first_name       VARCHAR(100) NOT NULL,
    last_name        VARCHAR(100) NOT NULL,
    license_number   VARCHAR(50)  NOT NULL UNIQUE,
    email            VARCHAR(255) NOT NULL UNIQUE,
    phone            VARCHAR(10)  NOT NULL,
    specialization   VARCHAR(255) NOT NULL,
    rating           DOUBLE PRECISION NOT NULL,
    created_at       TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP
);
