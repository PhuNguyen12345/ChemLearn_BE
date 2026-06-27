CREATE TABLE IF NOT EXISTS payment_transactions (
    id UUID PRIMARY KEY,
    order_code BIGINT NOT NULL UNIQUE,
    amount BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    payment_link_id VARCHAR(255),
    checkout_url TEXT,
    qr_code TEXT,
    buyer_name VARCHAR(255),
    buyer_email VARCHAR(255),
    payos_request TEXT,
    payos_response TEXT,
    webhook_payload TEXT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_payment_transactions_order_code
    ON payment_transactions (order_code);