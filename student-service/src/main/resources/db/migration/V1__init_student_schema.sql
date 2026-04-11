-- Student service: students + documents (PostgreSQL)

CREATE TABLE students (
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    cnp             VARCHAR(13)  NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    phone           VARCHAR(10)  NOT NULL,
    address         VARCHAR(255) NOT NULL,
    status          VARCHAR(255) NOT NULL,
    registration_date TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP
);

CREATE INDEX idx_students_cnp ON students (cnp);
CREATE INDEX idx_students_email ON students (email);
CREATE INDEX idx_students_status ON students (status);

CREATE TABLE documents (
    id              BIGSERIAL PRIMARY KEY,
    student_id      BIGINT NOT NULL REFERENCES students (id),
    document_type   VARCHAR(255) NOT NULL,
    file_path       VARCHAR(500) NOT NULL,
    status          VARCHAR(255) NOT NULL,
    upload_date     TIMESTAMP NOT NULL
);
