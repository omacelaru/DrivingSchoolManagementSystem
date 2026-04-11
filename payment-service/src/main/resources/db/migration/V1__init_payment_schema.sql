-- Payment service (student_id / lesson_id are logical references to other services)

CREATE TABLE payments (
    id                 BIGSERIAL PRIMARY KEY,
    student_id         BIGINT NOT NULL,
    amount             DECIMAL(10, 2) NOT NULL,
    payment_method     VARCHAR(255),
    status             VARCHAR(255) NOT NULL,
    transaction_date   TIMESTAMP NOT NULL,
    transaction_id     VARCHAR(100) UNIQUE,
    lesson_id          BIGINT,
    notes              VARCHAR(500),
    created_at         TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP
);

CREATE INDEX idx_payments_student_id ON payments (student_id);
CREATE INDEX idx_payments_status ON payments (status);
CREATE INDEX idx_payments_transaction_date ON payments (transaction_date);
CREATE INDEX idx_payments_transaction_id ON payments (transaction_id);
CREATE INDEX idx_payments_lesson_id ON payments (lesson_id);
