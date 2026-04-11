-- Course <-> CourseTag @ManyToMany via courses_course_tags

CREATE TABLE course_tags (
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(50)  NOT NULL,
    name VARCHAR(200) NOT NULL,
    CONSTRAINT uk_course_tags_code UNIQUE (code)
);

CREATE TABLE courses_course_tags (
    course_id     BIGINT NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    course_tag_id BIGINT NOT NULL REFERENCES course_tags (id) ON DELETE CASCADE,
    PRIMARY KEY (course_id, course_tag_id)
);

CREATE INDEX idx_courses_course_tags_course_tag_id ON courses_course_tags (course_tag_id);

-- Seed tags (matches CourseTagDataInitializer; keeps DB usable before first app run)
INSERT INTO course_tags (code, name) VALUES
    ('INTENSIVE', 'Intensive programme'),
    ('EVENING', 'Evening slots'),
    ('WEEKEND', 'Weekend slots'),
    ('EXAM_PREP', 'Exam preparation');
