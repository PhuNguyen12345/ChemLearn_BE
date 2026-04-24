-- ==============================================================================
-- BẢN MIGRATION V1: CHEMLEARN CORE + VIRTUAL LAB + GAMIFICATION (BẢN GỘP HOÀN CHỈNH)
-- DBMS: PostgreSQL | Primary Key: UUID
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- PHẦN 1: CORE USERS & ROLES
-- ------------------------------------------------------------------------------
CREATE TABLE users (
                       id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       role VARCHAR(20) NOT NULL CHECK (role IN ('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_PARENT', 'ROLE_ADMIN')),
                       avatar_url TEXT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE parents (
                         user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                         phone_number VARCHAR(20),
                         job_title VARCHAR(100)
);

CREATE TABLE students (
                          user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                          grade_level INTEGER NOT NULL CHECK (grade_level BETWEEN 6 AND 12),
                          total_points INTEGER DEFAULT 0,
                          current_streak INTEGER DEFAULT 0,
                          last_active_date DATE, -- Dùng để tính toán Streak cắt hay nối
                          parent_id UUID REFERENCES parents(user_id) ON DELETE SET NULL,
                          school_name VARCHAR(255)
);

CREATE TABLE teachers (
                          user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                          bio TEXT,
                          specialization VARCHAR(100),
                          degree VARCHAR(100),
                          workplace VARCHAR(255)
);

-- ------------------------------------------------------------------------------
-- PHẦN 2: CLASSES & ENROLLMENTS
-- ------------------------------------------------------------------------------
CREATE TABLE classes (
                         id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         grade_level INTEGER NOT NULL CHECK (grade_level BETWEEN 6 AND 12),
                         teacher_id UUID REFERENCES teachers(user_id) ON DELETE SET NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE class_enrollments (
                                   id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                   class_id UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
                                   student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
                                   joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   UNIQUE(class_id, student_id)
);

-- ------------------------------------------------------------------------------
-- PHẦN 3: VIRTUAL LAB MASTER DATA
-- ------------------------------------------------------------------------------
CREATE TABLE inventory_item (
                                id VARCHAR(50) PRIMARY KEY,
                                name VARCHAR(100) NOT NULL,
                                type VARCHAR(50) NOT NULL,
                                state VARCHAR(20),
                                description TEXT,
                                icon_data VARCHAR(255),
                                properties VARCHAR(255)
);

CREATE TABLE chemical_reaction (
                                   reaction_key VARCHAR(100) PRIMARY KEY,
                                   liquid_content VARCHAR(100),
                                   solid_content VARCHAR(100),
                                   gas_content VARCHAR(100),
                                   liquid_color VARCHAR(50),
                                   precipitate_color VARCHAR(50),
                                   reaction_state VARCHAR(50),
                                   reaction_info JSONB
);

CREATE TABLE lab (
                     id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                     title VARCHAR(200) NOT NULL,
                     description TEXT,
                     category VARCHAR(50),
                     difficulty VARCHAR(20),
                     type VARCHAR(50) NOT NULL,
                     tag VARCHAR(50),
                     gradient VARCHAR(100),
                     icon_color VARCHAR(50),
                     thumbnail_url VARCHAR(255),
                     max_score INTEGER DEFAULT 0,
                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lab_configuration (
                                   id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                   lab_id UUID NOT NULL REFERENCES lab(id) ON DELETE CASCADE,
                                   config JSONB,
                                   viewport JSONB,
                                   initial_workspace JSONB
);

-- ------------------------------------------------------------------------------
-- PHẦN 4: LMS COURSEWORK (Chapters, Lessons, Quizzes, Questions)
-- ------------------------------------------------------------------------------
CREATE TABLE chapters (
                          id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                          title VARCHAR(200) NOT NULL,
                          description TEXT,
                          grade_level INTEGER NOT NULL,
                          order_index INTEGER DEFAULT 0,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lessons (
                         id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                         chapter_id UUID NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
                         lab_id UUID REFERENCES lab(id) ON DELETE SET NULL,
                         title VARCHAR(200) NOT NULL,
                         content_type VARCHAR(50) NOT NULL,
                         video_url TEXT,
                         text_content TEXT,
                         duration_minutes INTEGER DEFAULT 0,
                         order_index INTEGER DEFAULT 0,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quizzes (
                         id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                         title VARCHAR(200) NOT NULL,
                         description TEXT,
                         quiz_type VARCHAR(50) DEFAULT 'EXAM',
                         duration_minutes INTEGER,
                         created_by UUID REFERENCES teachers(user_id) ON DELETE SET NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE questions (
                           id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                           lesson_id UUID REFERENCES lessons(id) ON DELETE CASCADE,
                           quiz_id UUID REFERENCES quizzes(id) ON DELETE CASCADE,
                           content TEXT NOT NULL,
                           question_type VARCHAR(50) DEFAULT 'SINGLE_CHOICE',
                           explanation TEXT,
                           order_index INTEGER DEFAULT 0,
    -- Ràng buộc: Câu hỏi chỉ thuộc về Lesson HOẶC Quiz
                           CONSTRAINT chk_question_belongs_to CHECK (
                               (lesson_id IS NOT NULL AND quiz_id IS NULL) OR
                               (lesson_id IS NULL AND quiz_id IS NOT NULL)
                               )
);

CREATE TABLE answers (
                         id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                         question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
                         content TEXT NOT NULL,
                         is_correct BOOLEAN DEFAULT FALSE,
                         order_index INTEGER DEFAULT 0
);

-- ------------------------------------------------------------------------------
-- PHẦN 5: ATTEMPTS, PROGRESS & ASSIGNMENTS
-- ------------------------------------------------------------------------------
CREATE TABLE lesson_progress (
                                 id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                 student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
                                 lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
                                 is_completed BOOLEAN DEFAULT FALSE,
                                 is_locked BOOLEAN DEFAULT TRUE,
                                 last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 UNIQUE(student_id, lesson_id)
);

CREATE TABLE quiz_attempts (
                               id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                               student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
                               quiz_id UUID REFERENCES quizzes(id) ON DELETE CASCADE,
                               lesson_id UUID REFERENCES lessons(id) ON DELETE CASCADE,
                               status VARCHAR(50) NOT NULL,
                               score DECIMAL(5, 2) DEFAULT 0,
                               total_questions INTEGER NOT NULL DEFAULT 0,
                               correct_answers INTEGER NOT NULL DEFAULT 0,
                               started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               submitted_at TIMESTAMP,
    -- Ràng buộc: Phiếu thi chỉ thuộc về Quiz HOẶC Lesson Mini-quiz
                               CONSTRAINT chk_attempt_belongs_to CHECK (
                                   (quiz_id IS NOT NULL AND lesson_id IS NULL) OR
                                   (quiz_id IS NULL AND lesson_id IS NOT NULL)
                                   )
);

CREATE TABLE attempt_answers (
                                 id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                 attempt_id UUID NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
                                 question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
                                 selected_answer_id UUID REFERENCES answers(id) ON DELETE SET NULL,
                                 is_correct BOOLEAN NOT NULL
);

CREATE TABLE user_lab_progress (
                                   id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                   student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
                                   lab_id UUID NOT NULL REFERENCES lab(id) ON DELETE CASCADE,
                                   status VARCHAR(50) DEFAULT 'UNCOMPLETED',
                                   progress_percent INTEGER DEFAULT 0,
                                   current_score INTEGER DEFAULT 0,
                                   completed_actions JSONB,
                                   current_workspace JSONB,
                                   viewport JSONB,
                                   thumbnail_url VARCHAR(255),
                                   is_finished BOOLEAN DEFAULT FALSE,
                                   started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   submitted_at TIMESTAMP,
                                   last_edited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE class_assignments (
                                   id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                   class_id UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
                                   title VARCHAR(200) NOT NULL,
                                   lab_id UUID REFERENCES lab(id) ON DELETE CASCADE,
                                   quiz_id UUID REFERENCES quizzes(id) ON DELETE CASCADE,
                                   due_date TIMESTAMP,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Ràng buộc: Bài tập giao về nhà là Lab HOẶC Quiz
                                   CONSTRAINT chk_assignment_target CHECK (
                                       (lab_id IS NOT NULL AND quiz_id IS NULL) OR
                                       (lab_id IS NULL AND quiz_id IS NOT NULL)
                                       )
);

-- ------------------------------------------------------------------------------
-- PHẦN 6: GAMIFICATION (Badges, Quests, XP Logs)
-- ------------------------------------------------------------------------------
CREATE TABLE badges (
                        id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        description TEXT,
                        icon_url TEXT,
                        criteria_json JSONB
);

CREATE TABLE user_badges (
                             id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                             student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
                             badge_id UUID NOT NULL REFERENCES badges(id) ON DELETE CASCADE,
                             earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE xp_logs (
                         id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                         student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
                         amount INTEGER NOT NULL,
                         source VARCHAR(50) NOT NULL, -- 'QUIZ', 'LAB', 'QUEST', 'STREAK'
                         description TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quests (
                        id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                        title VARCHAR(200) NOT NULL,
                        action_type VARCHAR(50) NOT NULL, -- 'DO_LAB', 'READ_LESSON', 'PERFECT_QUIZ'
                        target_value INTEGER NOT NULL,
                        reward_xp INTEGER NOT NULL,
                        is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE student_quests (
                                id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
                                quest_id UUID NOT NULL REFERENCES quests(id) ON DELETE CASCADE,
                                current_progress INTEGER DEFAULT 0,
                                is_claimed BOOLEAN DEFAULT FALSE,
                                assigned_date DATE NOT NULL DEFAULT CURRENT_DATE,
                                UNIQUE(student_id, quest_id, assigned_date)
);