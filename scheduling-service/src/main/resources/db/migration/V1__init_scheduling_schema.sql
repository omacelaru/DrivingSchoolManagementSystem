-- Scheduling service: courses + lessons

CREATE TABLE courses (
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(200) NOT NULL,
    description        VARCHAR(1000),
    price              DECIMAL(10, 2) NOT NULL,
    instructor_id      BIGINT NOT NULL,
    vehicle_id         BIGINT NOT NULL,
    number_of_lessons  INTEGER NOT NULL,
    course_type        VARCHAR(255) NOT NULL,
    version            BIGINT,
    created_at         TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP
);

CREATE TABLE lessons (
    id                 BIGSERIAL PRIMARY KEY,
    student_id         BIGINT NOT NULL,
    course_id          BIGINT REFERENCES courses (id),
    start_time         TIMESTAMP NOT NULL,
    end_time           TIMESTAMP NOT NULL,
    status             VARCHAR(255) NOT NULL,
    created_at         TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP
);

CREATE INDEX idx_lessons_student_id ON lessons (student_id);
CREATE INDEX idx_lessons_start_time ON lessons (start_time);
CREATE INDEX idx_lessons_course_id ON lessons (course_id);
