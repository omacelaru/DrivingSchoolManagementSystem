-- OneToOne Student <-> StudentProfile; ElementCollection target licence categories (enum as STRING)

CREATE TABLE student_profiles (
    id                        BIGSERIAL PRIMARY KEY,
    student_id                BIGINT NOT NULL UNIQUE REFERENCES students (id) ON DELETE CASCADE,
    emergency_contact_name    VARCHAR(100),
    emergency_contact_phone   VARCHAR(10),
    notes                     VARCHAR(2000)
);

CREATE TABLE student_target_license_categories (
    student_id BIGINT NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    category   VARCHAR(10) NOT NULL,
    PRIMARY KEY (student_id, category)
);
