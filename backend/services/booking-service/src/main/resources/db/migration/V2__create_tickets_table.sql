CREATE TABLE tickets (
    id VARCHAR(36) NOT NULL,
    booking_id VARCHAR(36) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    customer_subject VARCHAR(255) NOT NULL,
    ticket_number VARCHAR(100) NOT NULL UNIQUE,
    qr_code_token VARCHAR(512) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    checked_in_at DATETIME(6) NULL,
    checked_in_by VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_tickets_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_tickets_booking_id ON tickets (booking_id);
CREATE INDEX idx_tickets_event_id ON tickets (event_id);
CREATE INDEX idx_tickets_qr_token ON tickets (qr_code_token);
CREATE INDEX idx_tickets_status ON tickets (status);
