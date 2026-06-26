-- ============================================================
-- V15: Add target_graduation_year for dynamic grade calculation
-- Strategy: Dual-Write — keep grade_level for backward compatibility
-- ============================================================

-- Step 1: Add the new column (nullable initially for safe rollout)
ALTER TABLE students ADD COLUMN target_graduation_year INTEGER;

-- Step 2: Backfill target_graduation_year from existing grade_level
-- Vietnamese academic year starts in July (month >= 7 = new school year)
--
-- If current month >= 7 (new school year has started):
--   target_graduation_year = current_year + (9 - grade_level) + 1
--   Example (Aug 2026): grade 8 → 2026 + (9-8) + 1 = 2028
--
-- If current month < 7 (still in current school year):
--   target_graduation_year = current_year + (9 - grade_level)
--   Example (Jun 2026): grade 8 → 2026 + (9-8) = 2027

UPDATE students
SET target_graduation_year =
    CASE
        WHEN EXTRACT(MONTH FROM NOW()) >= 7
            THEN EXTRACT(YEAR FROM NOW())::INT + (9 - grade_level) + 1
        ELSE
            EXTRACT(YEAR FROM NOW())::INT + (9 - grade_level)
    END
WHERE grade_level IS NOT NULL
  AND grade_level BETWEEN 6 AND 9
  AND target_graduation_year IS NULL;

-- Step 3: Add a comment for documentation
COMMENT ON COLUMN students.target_graduation_year IS
    'The calendar year the student is expected to graduate from THCS (grade 9). '
    'Used to dynamically calculate current grade level based on server time. '
    'Formula: if month>=7: grade = 9 - (target_year - current_year) + 1, '
    'else: grade = 9 - (target_year - current_year). '
    'See GradeCalculator.java for the canonical implementation.';
