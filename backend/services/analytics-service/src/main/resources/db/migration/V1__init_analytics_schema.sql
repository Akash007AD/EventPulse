-- ==============================================================================
-- Milestone 6: Analytics Service Real-Time Projections Schema
-- ==============================================================================

CREATE TABLE IF NOT EXISTS event_analytics (
    id VARCHAR(36) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    event_title VARCHAR(255) NULL,
    total_revenue DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    tickets_sold INT NOT NULL DEFAULT 0,
    confirmed_bookings INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_event_analytics PRIMARY KEY (id),
    CONSTRAINT uk_event_analytics_event_id UNIQUE (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_event_analytics_event_id ON event_analytics (event_id);

CREATE TABLE IF NOT EXISTS processed_events (
    event_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    processed_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_processed_events PRIMARY KEY (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
