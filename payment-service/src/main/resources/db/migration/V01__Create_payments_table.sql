-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transaction_id VARCHAR(100),
    invoice_id BIGINT,
    course_id BIGINT,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP,
    CONSTRAINT fk_payment_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL
);

-- Create unique index for transaction_id (only for non-null values)
CREATE UNIQUE INDEX IF NOT EXISTS idx_transaction_id_unique ON payments(transaction_id) WHERE transaction_id IS NOT NULL;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_student_id ON payments(student_id);
CREATE INDEX IF NOT EXISTS idx_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_transaction_date ON payments(transaction_date);
CREATE INDEX IF NOT EXISTS idx_transaction_id ON payments(transaction_id);
CREATE INDEX IF NOT EXISTS idx_invoice_id ON payments(invoice_id);

