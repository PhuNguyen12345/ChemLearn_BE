-- Consolidated Flyway migration V5: Update quest rewards and add image_url to map_islands
-- 1. Update default daily quest reward coins to total exactly 1500 coins per day
UPDATE quests SET reward_coins = 300 WHERE action_type = 'DO_LAB';
UPDATE quests SET reward_coins = 300 WHERE action_type = 'LEARN_LESSON';
UPDATE quests SET reward_coins = 200 WHERE action_type = 'LOGIN';
UPDATE quests SET reward_coins = 250 WHERE action_type = 'FEED_PET';
UPDATE quests SET reward_coins = 450 WHERE action_type = 'PLAY_PVP';

-- 2. Add image_url to map_islands and populate images
ALTER TABLE map_islands ADD COLUMN IF NOT EXISTS image_url TEXT;

UPDATE map_islands SET image_url = 'https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/StarterIsland.png' WHERE name = 'Quần đảo Nhập môn';
UPDATE map_islands SET image_url = 'https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/BondingKingdom.png' WHERE name = 'Vương quốc Liên kết' OR name = 'Vương Quốc Liên Kết';
UPDATE map_islands SET image_url = 'https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/AxitSea.png' WHERE name = 'Đại dương Axit' OR name = 'Biển Axit';
