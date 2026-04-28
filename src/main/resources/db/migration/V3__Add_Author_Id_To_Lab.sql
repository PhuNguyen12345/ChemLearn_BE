-- Bước 1: Thêm cột author_id
ALTER TABLE lab
    ADD COLUMN author_id UUID;

-- Bước 2: Thiết lập KHÓA NGOẠI (Foreign Key Constraint)
ALTER TABLE lab
    ADD CONSTRAINT fk_labs_author
    FOREIGN KEY (author_id)
    REFERENCES users (id)
    ON DELETE CASCADE; -- (Tùy chọn) Nếu xóa User, tự động xóa luôn các bài Lab Sandbox của User đó