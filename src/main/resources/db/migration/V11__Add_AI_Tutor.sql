CREATE TABLE ai_chat_sessions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    grade_level INTEGER NOT NULL CHECK (grade_level BETWEEN 6 AND 9),
    book_type VARCHAR(20) NOT NULL CHECK (book_type IN ('KNTT', 'CTST', 'CD')),
    topic VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE ai_chat_messages (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES ai_chat_sessions(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ASSISTANT')),
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE ai_response_cache (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    cache_key VARCHAR(128) NOT NULL UNIQUE,
    normalized_question TEXT NOT NULL,
    grade_level INTEGER NOT NULL CHECK (grade_level BETWEEN 6 AND 9),
    book_type VARCHAR(20) NOT NULL CHECK (book_type IN ('KNTT', 'CTST', 'CD')),
    topic VARCHAR(255),
    answer TEXT NOT NULL,
    suggested_labs JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE student_topic_mastery (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    grade_level INTEGER NOT NULL CHECK (grade_level BETWEEN 6 AND 9),
    book_type VARCHAR(20) NOT NULL CHECK (book_type IN ('KNTT', 'CTST', 'CD')),
    topic VARCHAR(255) NOT NULL,
    correct_count INTEGER NOT NULL DEFAULT 0,
    total_count INTEGER NOT NULL DEFAULT 0,
    last_accuracy DOUBLE PRECISION NOT NULL DEFAULT 0,
    mastery_level VARCHAR(20) NOT NULL CHECK (mastery_level IN ('WEAK', 'MEDIUM', 'GOOD')),
    wrong_topics JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE ai_generated_exams (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    grade_level INTEGER NOT NULL CHECK (grade_level BETWEEN 6 AND 9),
    book_type VARCHAR(20) NOT NULL CHECK (book_type IN ('KNTT', 'CTST', 'CD')),
    exam_type VARCHAR(40) NOT NULL CHECK (exam_type IN ('QUIZ_15_MIN', 'FORTY_FIVE_MINUTES', 'MIDTERM', 'FINAL')),
    topic VARCHAR(255) NOT NULL,
    difficulty VARCHAR(20) NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'MIXED')),
    title VARCHAR(255) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    questions JSONB NOT NULL,
    answer_key JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE curriculum_lessons (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    grade_level INTEGER NOT NULL CHECK (grade_level BETWEEN 6 AND 9),
    book_type VARCHAR(20) NOT NULL CHECK (book_type IN ('KNTT', 'CTST', 'CD')),
    topic VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    lesson_id UUID REFERENCES lessons(id) ON DELETE SET NULL,
    lab_id UUID REFERENCES lab(id) ON DELETE SET NULL,
    published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_ai_chat_sessions_student ON ai_chat_sessions(student_id);
CREATE INDEX idx_ai_chat_messages_session ON ai_chat_messages(session_id);
CREATE INDEX idx_ai_response_cache_lookup ON ai_response_cache(cache_key);
CREATE INDEX idx_student_topic_mastery_student_topic ON student_topic_mastery(student_id, grade_level, book_type, topic);
CREATE INDEX idx_ai_generated_exams_student ON ai_generated_exams(student_id);
CREATE INDEX idx_curriculum_lessons_lookup ON curriculum_lessons(grade_level, book_type, topic);
