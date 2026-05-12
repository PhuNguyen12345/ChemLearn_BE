CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT accounts_role_check CHECK (role IN ('ROLE_ADMIN', 'ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_PARENT'))
);

ALTER TABLE IF EXISTS accounts DROP CONSTRAINT IF EXISTS accounts_role_check;
ALTER TABLE IF EXISTS accounts
    ADD CONSTRAINT accounts_role_check
    CHECK (role IN ('ROLE_ADMIN', 'ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_PARENT'));

CREATE TABLE IF NOT EXISTS chapters (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL DEFAULT 0,
    published BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS lessons (
    id BIGSERIAL PRIMARY KEY,
    chapter_id BIGINT NOT NULL REFERENCES chapters(id),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    estimated_minutes INTEGER,
    display_order INTEGER NOT NULL DEFAULT 0,
    published BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS mini_quiz_questions (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES lessons(id),
    prompt TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_option VARCHAR(1) NOT NULL,
    explanation TEXT
);

CREATE TABLE IF NOT EXISTS quizzes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    quiz_type VARCHAR(30) NOT NULL,
    duration_minutes INTEGER,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    published BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT REFERENCES accounts(id),
    class_id BIGINT REFERENCES classes(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS classes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    class_code VARCHAR(6) NOT NULL UNIQUE,
    schedule VARCHAR(200),
    description TEXT,
    teacher_id BIGINT REFERENCES accounts(id)
);

CREATE TABLE IF NOT EXISTS class_chapters (
    class_id BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    chapter_id BIGINT NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    PRIMARY KEY (class_id, chapter_id)
);

CREATE TABLE IF NOT EXISTS class_students (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES accounts(id),
    UNIQUE (class_id, student_id)
);

CREATE TABLE IF NOT EXISTS class_assignments (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    class_id UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    lab_id UUID REFERENCES lab(id) ON DELETE CASCADE,
    quiz_id UUID REFERENCES quizzes(id) ON DELETE CASCADE,
    due_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_assignment_target CHECK (
        (lab_id IS NOT NULL AND quiz_id IS NULL) OR
        (lab_id IS NULL AND quiz_id IS NOT NULL)
    )
);

CREATE TABLE IF NOT EXISTS question_bank_items (
    id BIGSERIAL PRIMARY KEY,
    created_by BIGINT NOT NULL REFERENCES accounts(id),
    question_type VARCHAR(50) NOT NULL DEFAULT 'SINGLE_CHOICE',
    prompt TEXT NOT NULL,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    correct_option VARCHAR(255),
    explanation TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS quiz_questions (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id),
    question_type VARCHAR(50) NOT NULL DEFAULT 'SINGLE_CHOICE',
    prompt TEXT NOT NULL,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    correct_option VARCHAR(255),
    explanation TEXT,
    display_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id),
    student_id BIGINT NOT NULL REFERENCES accounts(id),
    status VARCHAR(30) NOT NULL,
    score INTEGER NOT NULL DEFAULT 0,
    total_questions INTEGER NOT NULL DEFAULT 0,
    correct_answers INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS attempt_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL REFERENCES quiz_attempts(id),
    question_id BIGINT NOT NULL REFERENCES quiz_questions(id),
    selected_option TEXT,
    correct BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS parent_student_links (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT NOT NULL REFERENCES accounts(id),
    student_id BIGINT NOT NULL REFERENCES accounts(id),
    UNIQUE (parent_id, student_id)
);
