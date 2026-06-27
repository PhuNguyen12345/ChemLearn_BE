CREATE TABLE IF NOT EXISTS bi_companion_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    sender_name VARCHAR(100) NOT NULL,
    title VARCHAR(180) NOT NULL,
    message TEXT NOT NULL,
    message_type VARCHAR(40) NOT NULL,
    scheduled_for DATE NOT NULL,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_bi_companion_messages_student_slot UNIQUE (student_id, message_type, scheduled_for)
);

CREATE INDEX IF NOT EXISTS idx_bi_companion_messages_student_created
    ON bi_companion_messages(student_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_bi_companion_messages_scheduled_for
    ON bi_companion_messages(scheduled_for);
