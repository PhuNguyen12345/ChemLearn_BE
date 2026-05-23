ALTER TABLE lab
    ADD CONSTRAINT fk_labs_author
    FOREIGN KEY (author_id)
    REFERENCES users (id)
    ON DELETE CASCADE; -- (Tùy chọn) Nếu xóa User, tự động xóa luôn các bài Lab Sandbox của User đó