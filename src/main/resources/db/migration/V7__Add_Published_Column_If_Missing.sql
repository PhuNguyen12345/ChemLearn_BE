-- ==============================================================================
-- MIGRATION V7: ENSURE PUBLISHED COLUMN EXISTS FOR STUDY ZONE
-- DBMS: PostgreSQL
-- ==============================================================================

-- Add published column to chapters if it doesn't exist
ALTER TABLE chapters
ADD COLUMN IF NOT EXISTS published BOOLEAN DEFAULT TRUE;

-- Add published column to lessons if it doesn't exist
ALTER TABLE lessons
ADD COLUMN IF NOT EXISTS published BOOLEAN DEFAULT TRUE;

-- Update any NULL values to TRUE to ensure published content is visible
UPDATE chapters SET published = TRUE WHERE published IS NULL;
UPDATE lessons SET published = TRUE WHERE published IS NULL;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_chapters_published_scope ON chapters(published, material_scope);
CREATE INDEX IF NOT EXISTS idx_lessons_published_scope ON lessons(published, material_scope);
CREATE INDEX IF NOT EXISTS idx_chapters_published_created_by ON chapters(published, created_by);
CREATE INDEX IF NOT EXISTS idx_lessons_published_created_by ON lessons(published, created_by);
