-- ==============================================================================
-- MIGRATION V5: ADD ADMIN CONTENT MANAGEMENT METADATA
-- DBMS: PostgreSQL
-- ==============================================================================

-- Add created_by and updated_by columns to chapters table
ALTER TABLE chapters 
ADD COLUMN IF NOT EXISTS created_by UUID REFERENCES users(id) ON DELETE SET NULL,
ADD COLUMN IF NOT EXISTS updated_by UUID REFERENCES users(id) ON DELETE SET NULL;


-- Add created_by and updated_by columns to lessons table
ALTER TABLE lessons
ADD COLUMN IF NOT EXISTS created_by UUID REFERENCES users(id) ON DELETE SET NULL,
ADD COLUMN IF NOT EXISTS updated_by UUID REFERENCES users(id) ON DELETE SET NULL;

-- Add created_by column to mini_quiz_questions table
ALTER TABLE mini_quiz_questions
ADD COLUMN IF NOT EXISTS created_by UUID REFERENCES users(id) ON DELETE SET NULL;

-- Create indexes for created_by for better query performance
CREATE INDEX IF NOT EXISTS idx_chapters_created_by ON chapters(created_by);
CREATE INDEX IF NOT EXISTS idx_lessons_created_by ON lessons(created_by);
CREATE INDEX IF NOT EXISTS idx_chapters_published ON chapters(published);
CREATE INDEX IF NOT EXISTS idx_lessons_published ON lessons(published);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
