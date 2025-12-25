-- Create lessons table
CREATE TABLE IF NOT EXISTS lessons (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    instructor_id BIGINT NOT NULL,
    vehicle_id BIGINT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP,
    CONSTRAINT fk_lesson_instructor FOREIGN KEY (instructor_id) REFERENCES instructors(id) ON DELETE RESTRICT
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_student_id ON lessons(student_id);
CREATE INDEX IF NOT EXISTS idx_instructor_id ON lessons(instructor_id);
CREATE INDEX IF NOT EXISTS idx_start_time ON lessons(start_time);

