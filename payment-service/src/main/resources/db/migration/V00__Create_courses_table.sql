-- Create courses table
CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    price NUMERIC(10, 2) NOT NULL,
    duration INTEGER NOT NULL,
    category VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP
);

-- Create indexes for courses
CREATE INDEX IF NOT EXISTS idx_course_category ON courses(category);
CREATE INDEX IF NOT EXISTS idx_course_active ON courses(active);

