ALTER TABLE learning_packages
    ALTER COLUMN grade_level DROP NOT NULL;

ALTER TABLE chapters
    ADD COLUMN IF NOT EXISTS need_purchase BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE learning_packages
SET package_name = CONCAT('Chemlearn edu ', grade_level),
    description = CONCAT('Access to Chemlearn Study Zone grade ', grade_level),
    base_price = 400000,
    duration_days = 365,
    benefits_json = CONCAT('{"studyZoneAccess":{"gradeLevel":', grade_level, '}}'),
    is_active = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE package_code IN ('GRADE_6', 'GRADE_7', 'GRADE_8', 'GRADE_9');

INSERT INTO learning_packages (
    id,
    package_code,
    grade_level,
    package_name,
    description,
    base_price,
    duration_days,
    benefits_json,
    is_active,
    created_at,
    updated_at
)
VALUES (
    '55555555-5555-5555-5555-555555555555',
    'CHEMLEARN_PLUS',
    NULL,
    'Chemlearn plus',
    'Draft package. Perks are not decided yet.',
    NULL,
    NULL,
    '{}',
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (package_code) DO UPDATE SET
    grade_level = EXCLUDED.grade_level,
    package_name = EXCLUDED.package_name,
    description = EXCLUDED.description,
    base_price = EXCLUDED.base_price,
    duration_days = EXCLUDED.duration_days,
    benefits_json = EXCLUDED.benefits_json,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

UPDATE chapters
SET need_purchase = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE material_scope = 'GLOBAL'
  AND published = TRUE
  AND grade_level IN (6, 7, 8, 9);
