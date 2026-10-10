CREATE TABLE organizer_applications (
    id VARCHAR(36) NOT NULL,
    user_subject VARCHAR(128) NOT NULL,
    organization_name VARCHAR(128) NOT NULL,
    contact_email VARCHAR(128) NOT NULL,
    contact_phone VARCHAR(32),
    business_registration_number VARCHAR(64),
    website_url VARCHAR(255),
    description VARCHAR(2000),
    status VARCHAR(32) NOT NULL,
    rejection_reason VARCHAR(500),
    reviewed_by VARCHAR(128),
    reviewed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_org_app_user ON organizer_applications (user_subject);
CREATE INDEX idx_org_app_status ON organizer_applications (status);
