-- Extend Order entity with checkout fields

-- Contact info
ALTER TABLE orders ADD COLUMN first_name VARCHAR(100);
ALTER TABLE orders ADD COLUMN last_name VARCHAR(100);
ALTER TABLE orders ADD COLUMN phone VARCHAR(20);

-- Structured address
ALTER TABLE orders ADD COLUMN street VARCHAR(255);
ALTER TABLE orders ADD COLUMN house_number VARCHAR(20);
ALTER TABLE orders ADD COLUMN city VARCHAR(100);
ALTER TABLE orders ADD COLUMN postal_code VARCHAR(20);
ALTER TABLE orders ADD COLUMN country VARCHAR(2) DEFAULT 'SK';

-- Company fields
ALTER TABLE orders ADD COLUMN is_company BOOLEAN DEFAULT false;
ALTER TABLE orders ADD COLUMN company_name VARCHAR(255);
ALTER TABLE orders ADD COLUMN ico VARCHAR(20);
ALTER TABLE orders ADD COLUMN dic VARCHAR(20);
ALTER TABLE orders ADD COLUMN ic_dph VARCHAR(20);
