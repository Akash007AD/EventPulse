-- ==============================================================================
-- Milestone 6: Notification Service Delivery Queue Schema
-- ==============================================================================

CREATE TABLE IF NOT EXISTS notification_deliveries (
    id VARCHAR(36) NOT NULL,
    recipient_subject VARCHAR(255) NOT NULL,
    recipient_email VARCHAR(255) NULL,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    last_error VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_notification_deliveries PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notifications_recipient ON notification_deliveries (recipient_subject);
CREATE INDEX idx_notifications_status ON notification_deliveries (status);
