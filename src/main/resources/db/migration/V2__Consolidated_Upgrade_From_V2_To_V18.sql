-- ============================================================================
-- CONSOLIDATED MIGRATION: V2 TO V18
-- Base schema assumed from V1.
-- This file replaces the incremental V2-V18 migrations.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- V2 / V3 / V4: Class and lab metadata
-- ----------------------------------------------------------------------------
ALTER TABLE classes
    ADD COLUMN IF NOT EXISTS schedule VARCHAR(200),
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS class_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS class_code VARCHAR(6);

DO $$
DECLARE
    class_row RECORD;
    generated_code TEXT;
BEGIN
    FOR class_row IN
        SELECT id
        FROM classes
        WHERE class_code IS NULL OR class_code = ''
    LOOP
        LOOP
            generated_code := LPAD((FLOOR(RANDOM() * 1000000))::INT::TEXT, 6, '0');
            EXIT WHEN NOT EXISTS (
                SELECT 1
                FROM classes
                WHERE class_code = generated_code
            );
        END LOOP;

        UPDATE classes
        SET class_code = generated_code
        WHERE id = class_row.id;
    END LOOP;
END $$;

ALTER TABLE classes
    ALTER COLUMN class_code SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS classes_class_code_key ON classes (class_code);

ALTER TABLE lab
    ADD COLUMN IF NOT EXISTS author_id UUID,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS class_chapters (
    class_id UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    chapter_id UUID NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    PRIMARY KEY (class_id, chapter_id)
);

-- ----------------------------------------------------------------------------
-- V5 / V6 / V7: Content management and visibility metadata
-- ----------------------------------------------------------------------------
ALTER TABLE chapters
    ADD COLUMN IF NOT EXISTS created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS material_scope VARCHAR(30) NOT NULL DEFAULT 'GLOBAL',
    ADD COLUMN IF NOT EXISTS owner_class_id UUID REFERENCES classes(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS published BOOLEAN DEFAULT TRUE;

ALTER TABLE lessons
    ADD COLUMN IF NOT EXISTS created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS material_scope VARCHAR(30) NOT NULL DEFAULT 'GLOBAL',
    ADD COLUMN IF NOT EXISTS owner_class_id UUID REFERENCES classes(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS published BOOLEAN DEFAULT TRUE;

UPDATE chapters
SET material_scope = 'GLOBAL'
WHERE material_scope IS NULL;

UPDATE lessons
SET material_scope = 'GLOBAL'
WHERE material_scope IS NULL;

UPDATE lessons l
SET material_scope = c.material_scope,
    owner_class_id = c.owner_class_id
FROM chapters c
WHERE l.chapter_id = c.id;

CREATE INDEX IF NOT EXISTS idx_chapters_created_by ON chapters(created_by);
CREATE INDEX IF NOT EXISTS idx_lessons_created_by ON lessons(created_by);
CREATE INDEX IF NOT EXISTS idx_chapters_published ON chapters(published);
CREATE INDEX IF NOT EXISTS idx_lessons_published ON lessons(published);
CREATE INDEX IF NOT EXISTS idx_chapters_material_scope ON chapters(material_scope);
CREATE INDEX IF NOT EXISTS idx_chapters_owner_class_id ON chapters(owner_class_id);
CREATE INDEX IF NOT EXISTS idx_lessons_material_scope ON lessons(material_scope);
CREATE INDEX IF NOT EXISTS idx_lessons_owner_class_id ON lessons(owner_class_id);
CREATE INDEX IF NOT EXISTS idx_chapters_published_scope ON chapters(published, material_scope);
CREATE INDEX IF NOT EXISTS idx_lessons_published_scope ON lessons(published, material_scope);
CREATE INDEX IF NOT EXISTS idx_chapters_published_created_by ON chapters(published, created_by);
CREATE INDEX IF NOT EXISTS idx_lessons_published_created_by ON lessons(published, created_by);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    ADD COLUMN IF NOT EXISTS provider_subject VARCHAR(255),
    ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS lockout_until TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_failed_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS gender VARCHAR(20) DEFAULT NULL;

CREATE TABLE IF NOT EXISTS account_link_requests (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    initiator_id UUID NOT NULL,
    target_email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS parent_student_links (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    parent_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT parent_student_links_parent_id_student_id_key UNIQUE (parent_id, student_id)
);

CREATE TABLE IF NOT EXISTS class_students (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    class_id UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT class_students_class_id_student_id_key UNIQUE (class_id, student_id)
);

CREATE TABLE IF NOT EXISTS ranking_history (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    record_date DATE NOT NULL,
    rank_value INTEGER NOT NULL,
    score INTEGER
);

CREATE INDEX IF NOT EXISTS idx_ranking_history_date_cat
    ON ranking_history (record_date, category);

-- ----------------------------------------------------------------------------
-- V8 / V14 / V15: Question bank and quiz question schema
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS question_bank_items (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    created_by UUID NOT NULL REFERENCES users(id),
    question_type VARCHAR(50) NOT NULL DEFAULT 'SINGLE_CHOICE',
    prompt TEXT NOT NULL,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    correct_option VARCHAR(255),
    explanation TEXT,
    point_value DECIMAL(6, 2) NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS quiz_questions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    quiz_id UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    question_type VARCHAR(50) NOT NULL DEFAULT 'SINGLE_CHOICE',
    prompt TEXT NOT NULL,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    correct_option VARCHAR(255),
    explanation TEXT,
    point_value DECIMAL(6, 2) NOT NULL DEFAULT 1,
    display_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS mini_quiz_questions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    prompt TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL DEFAULT 'SINGLE_CHOICE',
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_option VARCHAR(255) NOT NULL,
    explanation TEXT,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL
);

-- ----------------------------------------------------------------------------
-- V9 / V10 / V11: Quiz linkage and lifecycle
-- ----------------------------------------------------------------------------
ALTER TABLE quizzes
    ADD COLUMN IF NOT EXISTS class_id UUID REFERENCES classes(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS start_time TIMESTAMP,
    ADD COLUMN IF NOT EXISTS end_time TIMESTAMP,
    ADD COLUMN IF NOT EXISTS published BOOLEAN DEFAULT TRUE;

ALTER TABLE quizzes
    DROP CONSTRAINT IF EXISTS quizzes_quiz_type_check;

ALTER TABLE quizzes
    ADD CONSTRAINT quizzes_quiz_type_check
    CHECK (quiz_type IN ('FREE', 'ASSIGNMENT', 'EXAM', 'MINI_QUIZ'));

DROP TABLE IF EXISTS assignments CASCADE;

ALTER TABLE quests
    ADD COLUMN IF NOT EXISTS reward_coins INTEGER DEFAULT 0;

-- ----------------------------------------------------------------------------
-- V12 / V13: Attempt answer grading model
-- ----------------------------------------------------------------------------
ALTER TABLE attempt_answers
    DROP CONSTRAINT IF EXISTS fk25c079ncy1idc9r4fj4ug6t6f,
    DROP CONSTRAINT IF EXISTS attempt_answers_question_id_fkey,
    DROP CONSTRAINT IF EXISTS attempt_answers_quiz_question_id_fkey;

ALTER TABLE attempt_answers
    DROP COLUMN IF EXISTS selected_answer_id,
    DROP COLUMN IF EXISTS question_id;

ALTER TABLE attempt_answers
    ADD COLUMN IF NOT EXISTS quiz_question_id UUID,
    ADD COLUMN IF NOT EXISTS selected_option TEXT,
    ADD COLUMN IF NOT EXISTS awarded_points DECIMAL(6, 2);

ALTER TABLE attempt_answers
    ALTER COLUMN is_correct SET DEFAULT FALSE;

UPDATE attempt_answers
SET is_correct = FALSE
WHERE is_correct IS NULL;

ALTER TABLE attempt_answers
    ALTER COLUMN is_correct SET NOT NULL;

ALTER TABLE attempt_answers
    ADD CONSTRAINT attempt_answers_quiz_question_id_fkey
        FOREIGN KEY (quiz_question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE;

-- ----------------------------------------------------------------------------
-- V16 / V17 / V18: Auth onboarding, student gamification, OTP flow
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS access_requests (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    additional_info TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS invites (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    token VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE students
    ADD COLUMN IF NOT EXISTS experience INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS coins INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS pvp_wins INTEGER DEFAULT 0;

CREATE TABLE IF NOT EXISTS items (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    item_type VARCHAR(50) NOT NULL,
    price_coins INTEGER NOT NULL,
    effect_value INTEGER,
    image_url TEXT
);

CREATE TABLE IF NOT EXISTS pet_species (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    element VARCHAR(50) NOT NULL,
    rarity VARCHAR(50) NOT NULL,
    base_hp INTEGER NOT NULL,
    base_damage INTEGER NOT NULL,
    hp_growth INTEGER NOT NULL,
    damage_growth INTEGER NOT NULL,
    skill_name VARCHAR(100),
    skill_description TEXT,
    image_url TEXT
);

CREATE TABLE IF NOT EXISTS egg_drop_rates (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    egg_item_id UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    pet_species_id UUID NOT NULL REFERENCES pet_species(id) ON DELETE CASCADE,
    drop_weight INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS student_items (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    item_id UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT student_items_student_id_item_id_key UNIQUE (student_id, item_id)
);

CREATE TABLE IF NOT EXISTS student_pets (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    species_id UUID NOT NULL REFERENCES pet_species(id) ON DELETE CASCADE,
    level INTEGER NOT NULL DEFAULT 1,
    experience INTEGER NOT NULL DEFAULT 0,
    star_level INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT student_pets_student_id_species_id_key UNIQUE (student_id, species_id)
);

CREATE TABLE IF NOT EXISTS student_pet_fragments (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    species_id UUID NOT NULL REFERENCES pet_species(id) ON DELETE CASCADE,
    amount INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT student_pet_fragments_student_id_species_id_key UNIQUE (student_id, species_id)
);

CREATE TABLE IF NOT EXISTS map_islands (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    unlock_level INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS map_nodes (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    island_id UUID NOT NULL REFERENCES map_islands(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    node_type VARCHAR(50) NOT NULL,
    target_id UUID,
    order_index INTEGER NOT NULL,
    xp_reward INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS student_node_progress (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    node_id UUID NOT NULL REFERENCES map_nodes(id) ON DELETE CASCADE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    stars INTEGER NOT NULL DEFAULT 0,
    completed_at TIMESTAMP,
    CONSTRAINT student_node_progress_student_id_node_id_key UNIQUE (student_id, node_id)
);

CREATE TABLE IF NOT EXISTS otp_verifications (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    pending_registration_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_otp_verifications_email_created_at
    ON otp_verifications (email, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_otp_verifications_email_code_unverified
    ON otp_verifications (email, otp_code)
    WHERE verified = FALSE;