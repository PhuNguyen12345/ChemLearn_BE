-- ==============================================================================
-- BẢN MIGRATION V17: ADD MISSING STUDENT GAMIFICATION FIELDS
-- ==============================================================================

ALTER TABLE students ADD COLUMN IF NOT EXISTS experience INTEGER DEFAULT 0;
ALTER TABLE students ADD COLUMN IF NOT EXISTS coins INTEGER DEFAULT 0;
ALTER TABLE students ADD COLUMN IF NOT EXISTS pvp_wins INTEGER DEFAULT 0;
