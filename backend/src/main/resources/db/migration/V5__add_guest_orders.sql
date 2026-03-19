-- Add guest order fields
ALTER TABLE orders ADD COLUMN guest_email VARCHAR(255);
ALTER TABLE orders ADD COLUMN guest_name VARCHAR(255);
ALTER TABLE orders ADD COLUMN shipping_address TEXT;

-- Make user_id nullable for guest orders
ALTER TABLE orders ALTER COLUMN user_id DROP NOT NULL;
