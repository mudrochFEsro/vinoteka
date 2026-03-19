-- Add version column for optimistic locking (race condition prevention)
ALTER TABLE products ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
