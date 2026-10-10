ALTER TABLE events ADD COLUMN poster_url VARCHAR(1000) NULL;
ALTER TABLE events ADD COLUMN seating_schema TEXT NULL;

CREATE TABLE event_performers (
    id VARCHAR(36) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255),
    image_url VARCHAR(1000),
    bio VARCHAR(2000),
    display_order INT DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_event_performers_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_event_performers_event_id ON event_performers (event_id);
