-- Flyway Migration V7: Update island unlock levels and set all node XP rewards to 1000
-- 1. Update island unlock levels
UPDATE map_islands SET unlock_level = 1 WHERE name = 'Quần đảo Nhập môn';
UPDATE map_islands SET unlock_level = 5 WHERE name = 'Vương quốc Liên kết' OR name = 'Vương Quốc Liên Kết';
UPDATE map_islands SET unlock_level = 9 WHERE name = 'Đại dương Axit' OR name = 'Biển Axit';

-- 2. Set all node XP rewards to 1000
UPDATE map_nodes SET xp_reward = 1000;
