-- Script SQL (Dành cho PostgreSQL)
-- Khởi tạo dữ liệu mẫu cho Hệ thống Thú cưng Gamification và Tài khoản Test

DO $$ 
DECLARE 
    v_student_id UUID;
    v_egg_id UUID := gen_random_uuid();
    v_food_id UUID := gen_random_uuid();
    
    v_pet1_id UUID := gen_random_uuid();
    v_pet2_id UUID := gen_random_uuid();
    v_pet3_id UUID := gen_random_uuid();
    v_pet4_id UUID := gen_random_uuid();
BEGIN
    -- =========================================================================
    -- PHẦN 1: TẠO HOẶC CẬP NHẬT TÀI KHOẢN HỌC SINH ĐỂ TEST
    -- =========================================================================
    SELECT id INTO v_student_id FROM users WHERE username = 'student';
    
    IF v_student_id IS NULL THEN
        -- Tạo User mới (Mật khẩu mặc định: 123456 được hash Bcrypt)
        v_student_id := gen_random_uuid();
        INSERT INTO users (id, username, email, password, full_name, role, is_active, created_at, updated_at)
        VALUES (
            v_student_id, 
            'student', 
            'student@example.com', 
            '$2a$12$KkQy2r9P2D1Y.O5YJbK.JODH6XgJ1p7rJmHlY7QzQ/5bY7bY7bY7b', -- hash của '123456'
            'Học sinh Test', 
            'STUDENT', 
            true, 
            CURRENT_TIMESTAMP, 
            CURRENT_TIMESTAMP
        );
        
        -- Khởi tạo thông tin Student (Cho sẵn 5000 Vàng)
        INSERT INTO students (user_id, grade_level, total_points, experience, current_streak, coins)
        VALUES (v_student_id, 8, 1000, 0, 0, 5000);
        
        RAISE NOTICE 'Đã tạo tài khoản student (Mật khẩu: 123456) với 5000 Vàng!';
    ELSE
        -- Nếu đã có tài khoản student, chỉ cần bơm 5000 vàng vào để test
        UPDATE students SET coins = 5000 WHERE user_id = v_student_id;
        RAISE NOTICE 'Đã cộng 5000 Vàng vào tài khoản student có sẵn!';
    END IF;

    -- =========================================================================
    -- PHẦN 2: DỮ LIỆU CỬA HÀNG VÀ PETS
    -- =========================================================================

    -- Xóa dữ liệu cũ (nếu có) để tránh lỗi trùng lặp khi chạy lại nhiều lần
    DELETE FROM egg_drop_rates;
    DELETE FROM student_items;
    DELETE FROM items;
    DELETE FROM student_pets;
    DELETE FROM pet_species;

    -- 1. Tạo Vật Phẩm (Items) trong Cửa hàng
    -- Trứng Linh Thú (EGG): Giá 500 Vàng
    INSERT INTO items (id, name, description, item_type, price_coins, effect_value, image_url)
    VALUES (v_egg_id, 'Trứng Linh Thú Tập Sự', 'Bao bọc bởi vầng hào quang kỳ bí. Mở ra để nhận 1 Thú Cưng ngẫu nhiên.', 'EGG', 500, NULL, NULL);

    -- Thức ăn (FOOD): Giá 50 Vàng, Tăng 500 EXP
    INSERT INTO items (id, name, description, item_type, price_coins, effect_value, image_url)
    VALUES (v_food_id, 'Bánh Táo Hóa Học', 'Món ăn yêu thích của mọi loại Thú Cưng. Cung cấp 500 EXP.', 'FOOD', 50, 500, NULL);

    -- 2. Tạo các Loại Thú Cưng (Pet Species)
    -- Skibidi Tolem (Common)
    INSERT INTO pet_species (id, name, element, rarity, base_hp, base_damage, hp_growth, damage_growth, skill_name, skill_description, image_url)
    VALUES (v_pet1_id, 'Skibidi Tolem', 'WATER', 'COMMON', 500, 50, 50, 5, 'Phun Nước', 'Gây sát thương hệ Thủy', NULL);

    -- Capybara Wizard (Rare)
    INSERT INTO pet_species (id, name, element, rarity, base_hp, base_damage, hp_growth, damage_growth, skill_name, skill_description, image_url)
    VALUES (v_pet2_id, 'Capybara Wizard', 'MAGIC', 'RARE', 800, 90, 80, 9, 'Phép Thuật Bình Tĩnh', 'Giảm sát thương nhận vào 20%', NULL);

    -- Doge Wizard (Epic)
    INSERT INTO pet_species (id, name, element, rarity, base_hp, base_damage, hp_growth, damage_growth, skill_name, skill_description, image_url)
    VALUES (v_pet3_id, 'Doge Wizard', 'LIGHT', 'EPIC', 1200, 150, 120, 15, 'Ánh Sáng Doge', 'Hồi phục 10% HP mỗi lượt', NULL);

    -- Tung Sahur Warrior (Legendary)
    INSERT INTO pet_species (id, name, element, rarity, base_hp, base_damage, hp_growth, damage_growth, skill_name, skill_description, image_url)
    VALUES (v_pet4_id, 'Tung Sahur Warrior', 'EARTH', 'LEGENDARY', 2500, 300, 250, 30, 'Địa Chấn Tối Thượng', 'Gây sát thương khủng khiếp lên mọi kẻ địch', NULL);

    -- 3. Cấu hình Tỉ lệ Rớt Trứng (Egg Drop Rates)
    -- Tổng tỉ lệ (Drop Weight) = 60 + 25 + 10 + 5 = 100
    INSERT INTO egg_drop_rates (id, egg_item_id, pet_species_id, drop_weight) VALUES (gen_random_uuid(), v_egg_id, v_pet1_id, 60);
    INSERT INTO egg_drop_rates (id, egg_item_id, pet_species_id, drop_weight) VALUES (gen_random_uuid(), v_egg_id, v_pet2_id, 25);
    INSERT INTO egg_drop_rates (id, egg_item_id, pet_species_id, drop_weight) VALUES (gen_random_uuid(), v_egg_id, v_pet3_id, 10);
    INSERT INTO egg_drop_rates (id, egg_item_id, pet_species_id, drop_weight) VALUES (gen_random_uuid(), v_egg_id, v_pet4_id, 5);

    RAISE NOTICE 'Thêm dữ liệu mẫu Pet System thành công!';
END $$;
