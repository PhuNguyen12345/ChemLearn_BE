-- Bước 1: Thêm cột author_id
ALTER TABLE lab
    ADD COLUMN author_id UUID;

-- Bước 2: Thiết lập KHÓA NGOẠI (Foreign Key Constraint)
ALTER TABLE lab
    ADD CONSTRAINT fk_labs_author
        FOREIGN KEY (author_id)
            REFERENCES users (id)
            ON DELETE CASCADE; -- (Tùy chọn) Nếu xóa User, tự động xóa luôn các bài Lab Sandbox của User đó



ALTER TABLE classes
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


