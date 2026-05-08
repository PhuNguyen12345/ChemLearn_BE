CREATE TABLE IF NOT EXISTS class_chapters (
    class_id uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    chapter_id uuid NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    PRIMARY KEY (class_id, chapter_id)
);