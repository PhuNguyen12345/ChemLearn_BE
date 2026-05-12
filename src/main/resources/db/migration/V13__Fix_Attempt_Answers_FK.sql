-- Migration V13: Force drop old constraints and link attempt_answers to quiz_questions

-- 1. Drop any potential legacy constraints that might still be pointing to the 'questions' table
ALTER TABLE attempt_answers DROP CONSTRAINT IF EXISTS fk25c079ncy1idc9r4fj4ug6t6f;
ALTER TABLE attempt_answers DROP CONSTRAINT IF EXISTS attempt_answers_question_id_fkey;
ALTER TABLE attempt_answers DROP CONSTRAINT IF EXISTS attempt_answers_quiz_question_id_fkey;

-- 2. Ensure data is clean to avoid violations
DELETE FROM attempt_answers;

-- 3. Add the correct constraint
ALTER TABLE attempt_answers ADD CONSTRAINT attempt_answers_quiz_question_id_fkey 
    FOREIGN KEY (quiz_question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE;
