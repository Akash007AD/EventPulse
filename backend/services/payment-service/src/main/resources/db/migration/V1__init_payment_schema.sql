-- ==============================================================================
-- Milestone 5: Payment Service & Transactional Outbox Schema
-- ==============================================================================

CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(36) NOT NULL,
    booking_id VARCHAR(36) NOT NULL,
    customer_subject VARCHAR(255) NOT NULL,
    idempotency_key VARCHAR(120) NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    razorpay_order_id VARCHAR(100) NULL,
    razorpay_payment_id VARCHAR(100) NULL,
    razorpay_signature VARCHAR(255) NULL,
    failure_reason VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT uk_payments_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT uk_payments_razorpay_payment_id UNIQUE (razorpay_payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_payments_booking_id ON payments (booking_id);
CREATE INDEX idx_payments_razorpay_order_id ON payments (razorpay_order_id);
CREATE INDEX idx_payments_customer_subject ON payments (customer_subject);

CREATE TABLE IF NOT EXISTS payment_outbox (
    id VARCHAR(36) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME(6) NOT NULL,
    published_at DATETIME(6) NULL,
    CONSTRAINT pk_payment_outbox PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_outbox_status_created ON payment_outbox (status, created_at);
