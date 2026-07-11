ALTER TABLE payment_transactions
    ADD COLUMN IF NOT EXISTS user_id UUID,
    ADD COLUMN IF NOT EXISTS package_code VARCHAR(50);

CREATE TABLE IF NOT EXISTS learning_packages (
    id UUID PRIMARY KEY,
    package_code VARCHAR(50) NOT NULL UNIQUE,
    grade_level INT NOT NULL,
    package_name VARCHAR(100) NOT NULL,
    description TEXT,
    base_price BIGINT,
    duration_days INT,
    benefits_json TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_package_entitlements (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    package_code VARCHAR(50) NOT NULL,
    payment_transaction_id UUID NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_package_entitlements_package
        FOREIGN KEY (package_code) REFERENCES learning_packages (package_code),
    CONSTRAINT fk_user_package_entitlements_payment
        FOREIGN KEY (payment_transaction_id) REFERENCES payment_transactions (id)
);

CREATE INDEX IF NOT EXISTS idx_payment_transactions_user_id ON payment_transactions (user_id);
CREATE INDEX IF NOT EXISTS idx_payment_transactions_package_code ON payment_transactions (package_code);
CREATE INDEX IF NOT EXISTS idx_user_package_entitlements_user_id ON user_package_entitlements (user_id);

INSERT INTO learning_packages (id, package_code, grade_level, package_name, description, base_price, duration_days, benefits_json, is_active)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'GRADE_6', 6, 'Gói Lớp 6', 'Gói học dành cho học sinh lớp 6', NULL, NULL, NULL, TRUE),
    ('22222222-2222-2222-2222-222222222222', 'GRADE_7', 7, 'Gói Lớp 7', 'Gói học dành cho học sinh lớp 7', NULL, NULL, NULL, TRUE),
    ('33333333-3333-3333-3333-333333333333', 'GRADE_8', 8, 'Gói Lớp 8', 'Gói học dành cho học sinh lớp 8', NULL, NULL, NULL, TRUE),
    ('44444444-4444-4444-4444-444444444444', 'GRADE_9', 9, 'Gói Lớp 9', 'Gói học dành cho học sinh lớp 9', NULL, NULL, NULL, TRUE)
ON CONFLICT (package_code) DO NOTHING;