-- ==============================================================================
-- MIGRATION V6: MATERIAL SCOPE FOR CLASS-PRIVATE CONTENT
-- DBMS: PostgreSQL
-- ==============================================================================

ALTER TABLE chapters
ADD COLUMN IF NOT EXISTS material_scope VARCHAR(30) NOT NULL DEFAULT 'GLOBAL',
ADD COLUMN IF NOT EXISTS owner_class_id UUID REFERENCES classes(id) ON DELETE SET NULL;

ALTER TABLE lessons
ADD COLUMN IF NOT EXISTS material_scope VARCHAR(30) NOT NULL DEFAULT 'GLOBAL',
ADD COLUMN IF NOT EXISTS owner_class_id UUID REFERENCES classes(id) ON DELETE SET NULL;

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

CREATE INDEX IF NOT EXISTS idx_chapters_material_scope ON chapters(material_scope);
CREATE INDEX IF NOT EXISTS idx_chapters_owner_class_id ON chapters(owner_class_id);
CREATE INDEX IF NOT EXISTS idx_lessons_material_scope ON lessons(material_scope);
CREATE INDEX IF NOT EXISTS idx_lessons_owner_class_id ON lessons(owner_class_id);
