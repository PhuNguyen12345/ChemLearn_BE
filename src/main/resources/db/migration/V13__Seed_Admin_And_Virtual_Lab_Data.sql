-- Required production seed data for admin access and the virtual lab catalog.
-- This migration is intentionally idempotent at row level so a deployed DB can
-- receive missing seed data without relying on Java CommandLineRunner lifecycle.

INSERT INTO users (
    username,
    email,
    password,
    full_name,
    role,
    is_active,
    auth_provider,
    failed_login_attempts,
    lockout_until,
    last_failed_at,
    created_at,
    updated_at
)
VALUES (
    'duckhisuu',
    'duckhisuu@chemlearn.local',
    '$2a$10$DyHb4R1Q6kcOX2z.4geIh.VKVN2fAvZl88qG9zi8rRlR6jmsTBQmK',
    'Duck Hisuu',
    'ROLE_ADMIN',
    TRUE,
    'LOCAL',
    0,
    NULL,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (username) DO UPDATE
SET password = EXCLUDED.password,
    role = 'ROLE_ADMIN',
    is_active = TRUE,
    auth_provider = 'LOCAL',
    failed_login_attempts = 0,
    lockout_until = NULL,
    last_failed_at = NULL,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO inventory_item (
    item_code,
    name,
    type,
    state,
    description,
    properties,
    sub_category,
    icon_name,
    icon_color,
    icon_fill
)
VALUES
    ('beaker', 'Cốc thủy tinh', 'CONTAINER', NULL, 'Dụng cụ chứa dung dịch.', '{}'::jsonb, NULL, 'Beaker', 'text-cyan-500', '#e0f2fe'),
    ('test_tube', 'Ống nghiệm', 'CONTAINER', NULL, 'Ống nghiệm dùng cho phản ứng nhỏ.', '{}'::jsonb, NULL, 'TestTube', 'text-violet-500', '#ede9fe'),
    ('bunsen_burner', 'Đèn Bunsen', 'EQUIPMENT', NULL, 'Nguồn nhiệt cho thí nghiệm.', '{}'::jsonb, NULL, 'Flame', 'text-orange-500', '#fed7aa'),
    ('zn_grain', 'Kẽm hạt', 'CHEMICAL', 'SOLID', 'Kim loại kẽm dạng hạt.', '{}'::jsonb, 'METAL', 'CircleDot', 'text-slate-500', '#cbd5e1'),
    ('sodium', 'Natri', 'CHEMICAL', 'SOLID', 'Kim loại natri.', '{}'::jsonb, 'METAL', 'Square', 'text-slate-400', '#e2e8f0'),
    ('water', 'Nước', 'CHEMICAL', 'LIQUID', 'Dung môi nước.', '{}'::jsonb, 'SOLVENT', 'Droplet', 'text-sky-500', '#bae6fd'),
    ('hcl', 'Axit HCl', 'CHEMICAL', 'LIQUID', 'Dung dịch axit clohidric.', '{}'::jsonb, 'ACID', 'Droplet', 'text-red-500', '#fecaca'),
    ('naoh_sol', 'Dung dịch NaOH', 'CHEMICAL', 'LIQUID', 'Dung dịch bazơ natri hiđroxit.', '{}'::jsonb, 'ALKALI', 'Droplet', 'text-blue-500', '#bfdbfe'),
    ('phenolphthalein', 'Phenolphthalein', 'CHEMICAL', 'LIQUID', 'Chất chỉ thị axit bazơ.', '{}'::jsonb, 'INDICATOR', 'Droplet', 'text-pink-500', '#fbcfe8'),
    ('litmus_paper', 'Giấy quỳ tím', 'CHEMICAL', 'SOLID', 'Chất chỉ thị nhận biết axit và bazơ.', '{}'::jsonb, 'INDICATOR', 'Square', 'text-purple-500', '#ddd6fe'),
    ('bacl2', 'Dung dịch BaCl2', 'CHEMICAL', 'LIQUID', 'Dung dịch bari clorua.', '{}'::jsonb, 'SALT_SOLUTION', 'Droplet', 'text-sky-600', '#dbeafe'),
    ('na2so4', 'Dung dịch Na2SO4', 'CHEMICAL', 'LIQUID', 'Dung dịch natri sunfat.', '{}'::jsonb, 'SALT_SOLUTION', 'Droplet', 'text-indigo-500', '#e0e7ff'),
    ('agno3', 'Dung dịch AgNO3', 'CHEMICAL', 'LIQUID', 'Dung dịch bạc nitrat.', '{}'::jsonb, 'SALT_SOLUTION', 'Droplet', 'text-gray-500', '#f3f4f6'),
    ('nacl', 'Natri clorua', 'CHEMICAL', 'SOLID', 'Muối ăn NaCl.', '{}'::jsonb, 'SALT_SOLID', 'CircleDot', 'text-slate-500', '#f8fafc')
ON CONFLICT (item_code) DO UPDATE
SET name = EXCLUDED.name,
    type = EXCLUDED.type,
    state = EXCLUDED.state,
    description = EXCLUDED.description,
    properties = EXCLUDED.properties,
    sub_category = EXCLUDED.sub_category,
    icon_name = EXCLUDED.icon_name,
    icon_color = EXCLUDED.icon_color,
    icon_fill = EXCLUDED.icon_fill;

WITH admin_user AS (
    SELECT id FROM users WHERE username = 'duckhisuu'
),
seed_labs(title, description, category, difficulty, type, max_score, allowed_chemicals) AS (
    VALUES
        (
            'Điều chế khí Hidro',
            'Thực hành phản ứng giữa kim loại kẽm (Zn) và axit clohidric (HCl) để sinh ra khí Hidro.',
            'KIM_LOAI',
            'EASY',
            'PREMADE',
            0,
            '["beaker","test_tube","bunsen_burner","zn_grain","hcl"]'::jsonb
        ),
        (
            'Tính chất hóa học của Nước',
            'Khảo sát phản ứng của Natri với nước và dùng Phenolphthalein để kiểm chứng dung dịch sinh ra có tính kiềm.',
            'GENERAL',
            'MEDIUM',
            'PREMADE',
            0,
            '["beaker","test_tube","bunsen_burner","sodium","water","phenolphthalein"]'::jsonb
        ),
        (
            'Phân loại chất bằng chất chỉ thị',
            'Dùng quỳ tím và Phenolphthalein để nhận biết môi trường axit HCl và bazơ NaOH.',
            'AXIT_BAZO',
            'EASY',
            'PREMADE',
            0,
            '["beaker","test_tube","hcl","naoh_sol","phenolphthalein","litmus_paper"]'::jsonb
        ),
        (
            'Phản ứng trao đổi trong dung dịch',
            'Quan sát phản ứng trao đổi tạo kết tủa đặc trưng với AgNO3 - NaCl và BaCl2 - Na2SO4.',
            'KET_TUA',
            'MEDIUM',
            'PREMADE',
            0,
            '["beaker","test_tube","bacl2","na2so4","agno3","nacl"]'::jsonb
        )
),
inserted_labs AS (
    INSERT INTO lab (
        title,
        description,
        category,
        difficulty,
        type,
        max_score,
        author_id,
        created_at,
        updated_at
    )
    SELECT
        seed_labs.title,
        seed_labs.description,
        seed_labs.category,
        seed_labs.difficulty,
        seed_labs.type,
        seed_labs.max_score,
        admin_user.id,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    FROM seed_labs
    CROSS JOIN admin_user
    WHERE NOT EXISTS (
        SELECT 1
        FROM lab existing
        WHERE existing.title = seed_labs.title
          AND existing.type = seed_labs.type
    )
    RETURNING id, title
),
all_seed_labs AS (
    SELECT lab.id, lab.title, seed_labs.allowed_chemicals
    FROM lab
    JOIN seed_labs ON seed_labs.title = lab.title AND seed_labs.type = lab.type
)
INSERT INTO lab_configuration (
    lab_id,
    config,
    viewport,
    initial_workspace
)
SELECT
    all_seed_labs.id,
    jsonb_build_object('allowed_chemicals', all_seed_labs.allowed_chemicals),
    '{"offset":{"x":0,"y":0},"zoom_scale":1.0}'::jsonb,
    '[]'::jsonb
FROM all_seed_labs
WHERE NOT EXISTS (
    SELECT 1
    FROM lab_configuration existing
    WHERE existing.lab_id = all_seed_labs.id
);
