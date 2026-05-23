-- Migration to add updated_at column to chapters table
ALTER TABLE chapters
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
