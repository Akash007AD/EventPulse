ALTER TABLE tickets ADD COLUMN seat_number VARCHAR(64) NULL;
CREATE INDEX idx_tickets_event_seat ON tickets (event_id, seat_number);

ALTER TABLE bookings ADD COLUMN seat_numbers VARCHAR(500) NULL;
