-- Flyway Migration V6: Add monster fields to map_nodes and create map_node_questions table

-- 1. Add monster fields to map_nodes
ALTER TABLE map_nodes ADD COLUMN IF NOT EXISTS monster_name VARCHAR(200);
ALTER TABLE map_nodes ADD COLUMN IF NOT EXISTS monster_image_url TEXT;
ALTER TABLE map_nodes ADD COLUMN IF NOT EXISTS monster_idle_url TEXT;
ALTER TABLE map_nodes ADD COLUMN IF NOT EXISTS monster_attack_url TEXT;
ALTER TABLE map_nodes ADD COLUMN IF NOT EXISTS monster_damaged_url TEXT;

-- 2. Create map_node_questions table
CREATE TABLE IF NOT EXISTS map_node_questions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    node_id UUID NOT NULL REFERENCES map_nodes(id) ON DELETE CASCADE,
    prompt TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_option VARCHAR(5) NOT NULL, -- 'A', 'B', 'C', 'D'
    explanation TEXT
);

-- 3. Add index for faster querying by node
CREATE INDEX IF NOT EXISTS idx_map_node_questions_node_id ON map_node_questions(node_id);
