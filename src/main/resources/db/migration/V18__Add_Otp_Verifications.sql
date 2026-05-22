CREATE TABLE IF NOT EXISTS otp_verifications (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    pending_registration_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_otp_verifications_email_created_at
    ON otp_verifications (email, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_otp_verifications_email_code_unverified
    ON otp_verifications (email, otp_code)
    WHERE verified = FALSE;
