-- Xóa khóa chính cũ (Vốn đang là chuỗi string 'id')
ALTER TABLE inventory_item DROP CONSTRAINT inventory_item_pkey;

-- Đổi tên cột id cũ thành item_code và đặt là duy nhất (Unique)
ALTER TABLE inventory_item RENAME COLUMN id TO item_code;
ALTER TABLE inventory_item ADD CONSTRAINT uk_inventory_item_code UNIQUE (item_code);

-- Thêm một cột id mới làm khóa chính tự tăng (BIGSERIAL cho PostgreSQL)
ALTER TABLE inventory_item ADD COLUMN id BIGSERIAL PRIMARY KEY;

-- Thêm các cột phân loại và đồ họa theo kiến trúc mới
ALTER TABLE inventory_item ADD COLUMN sub_category VARCHAR(50);
ALTER TABLE inventory_item ADD COLUMN icon_name VARCHAR(50);
ALTER TABLE inventory_item ADD COLUMN icon_color VARCHAR(50);
ALTER TABLE inventory_item ADD COLUMN icon_fill VARCHAR(50);

-- Loại bỏ cột icon_data chung chung cũ
ALTER TABLE inventory_item DROP COLUMN icon_data;

-- Đổi kiểu dữ liệu cột properties sang JSONB. Dùng NULLIF để tránh lỗi ép kiểu khi chuỗi rỗng
ALTER TABLE inventory_item ALTER COLUMN properties TYPE JSONB USING (NULLIF(properties, '')::jsonb);
