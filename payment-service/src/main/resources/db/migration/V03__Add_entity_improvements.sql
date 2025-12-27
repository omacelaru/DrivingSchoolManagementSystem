-- Add improvements to payments table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='payments' AND column_name='transaction_id') THEN
        ALTER TABLE payments ADD COLUMN transaction_id VARCHAR(100);
        CREATE UNIQUE INDEX IF NOT EXISTS idx_transaction_id_unique ON payments(transaction_id) WHERE transaction_id IS NOT NULL;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='payments' AND column_name='notes') THEN
        ALTER TABLE payments ADD COLUMN notes VARCHAR(500);
    END IF;
END $$;

-- Update invoice_id to be a foreign key
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name='fk_payment_invoice') THEN
        ALTER TABLE payments ADD CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Create new indexes for payments
CREATE INDEX IF NOT EXISTS idx_transaction_id ON payments(transaction_id);
CREATE INDEX IF NOT EXISTS idx_invoice_id ON payments(invoice_id);

-- Add improvements to invoices table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='invoices' AND column_name='due_date') THEN
        ALTER TABLE invoices ADD COLUMN due_date DATE NOT NULL DEFAULT (CURRENT_DATE + INTERVAL '30 days');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='invoices' AND column_name='paid_date') THEN
        ALTER TABLE invoices ADD COLUMN paid_date DATE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='invoices' AND column_name='description') THEN
        ALTER TABLE invoices ADD COLUMN description VARCHAR(1000);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='invoices' AND column_name='last_modified_date') THEN
        ALTER TABLE invoices ADD COLUMN last_modified_date TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='invoices' AND column_name='version') THEN
        ALTER TABLE invoices ADD COLUMN version BIGINT DEFAULT 0;
    END IF;
END $$;

-- Create indexes for invoices
CREATE INDEX IF NOT EXISTS idx_invoice_student_id ON invoices(student_id);
CREATE INDEX IF NOT EXISTS idx_invoice_status ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoice_number ON invoices(invoice_number);

-- Add improvements to courses table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='courses' AND column_name='active') THEN
        ALTER TABLE courses ADD COLUMN active BOOLEAN NOT NULL DEFAULT true;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='courses' AND column_name='version') THEN
        ALTER TABLE courses ADD COLUMN version BIGINT DEFAULT 0;
    END IF;
END $$;

-- Create indexes for courses
CREATE INDEX IF NOT EXISTS idx_course_category ON courses(category);
CREATE INDEX IF NOT EXISTS idx_course_active ON courses(active);

