-- Migration V12: Update attempt_answers table to support text-based selections and link to quiz_questions

-- 1. Clear existing attempt answers as they link to the old 'questions' bank which is no longer compatible with the new grading system
DELETE FROM attempt_answers;

-- 2. Rename column question_id to quiz_question_id and update reference
ALTER TABLE attempt_answers RENAME COLUMN question_id TO quiz_question_id;

-- 2. Drop the old foreign key constraint and add a new one pointing to quiz_questions
-- The error showed Hibernate's auto-generated name: fk25c079ncy1idc9r4fj4ug6t6f
ALTER TABLE attempt_answers DROP CONSTRAINT IF EXISTS fk25c079ncy1idc9r4fj4ug6t6f;
ALTER TABLE attempt_answers DROP CONSTRAINT IF EXISTS attempt_answers_question_id_fkey;

ALTER TABLE attempt_answers ADD CONSTRAINT attempt_answers_quiz_question_id_fkey 
    FOREIGN KEY (quiz_question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE;

-- 3. Drop selected_answer_id column
ALTER TABLE attempt_answers DROP COLUMN IF EXISTS selected_answer_id;

