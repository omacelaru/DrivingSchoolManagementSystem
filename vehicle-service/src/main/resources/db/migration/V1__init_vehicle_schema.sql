-- Vehicle service: vehicles + maintenances

CREATE TABLE vehicles (
    id               BIGSERIAL PRIMARY KEY,
    license_plate    VARCHAR(20) NOT NULL UNIQUE,
    make             VARCHAR(50)  NOT NULL,
    model            VARCHAR(50)  NOT NULL,
    year             INTEGER      NOT NULL,
    insurance_expiry DATE         NOT NULL,
    status           VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    last_modified_date TIMESTAMP
);

CREATE INDEX idx_vehicles_license_plate ON vehicles (license_plate);
CREATE INDEX idx_vehicles_status ON vehicles (status);

CREATE TABLE maintenances (
    id               BIGSERIAL PRIMARY KEY,
    vehicle_id       BIGINT NOT NULL REFERENCES vehicles (id),
    maintenance_date DATE NOT NULL,
    description      VARCHAR(500),
    cost             DOUBLE PRECISION NOT NULL,
    type             VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP NOT NULL
);
