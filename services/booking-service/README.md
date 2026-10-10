# EventPulse — Booking Service

The **Booking Service** manages ticket reservations, booking lifecycle states, hold windows, and inventory validation for EventPulse.

## Architecture & Technology Stack

- **Framework:** Spring Boot 3.4.3 on Java 21 LTS
- **Boilerplate Reduction:** Project Lombok
- **Persistence:** Spring Data JPA with MySQL 8.0+ Dialect
- **Database Migrations:** Flyway (`flyway-mysql`)
- **HTTP Client:** Spring 6 `RestClient` with timeout controls connecting to Event Service
- **Error Handling:** RFC 7807 `ProblemDetail`
- **Testing:** JUnit 5, Mockito, Spring Boot Test, H2 (MySQL mode for tests), Testcontainers MySQL

## Booking State Machine

```
              +-------------------+
              |  PENDING_PAYMENT  |
              +---------+---------+
                        |
       +----------------+----------------+
       | (Payment OK)   | (Cancel)       | (Hold Expires)
       v                v                v
+--------------+ +--------------+ +--------------+
|  CONFIRMED   | |  CANCELLED   | |   EXPIRED    |
+------+-------+ +--------------+ +--------------+
       | (Refund Request)
       v
+------------------+
|  REFUND_PENDING  |
+--------+---------+
         | (Refund Confirmed)
         v
+------------------+
|     REFUNDED     |
+------------------+
```

## Database Schema (MySQL)

Flyway migration `src/main/resources/db/migration/V1__create_bookings_table.sql`:

```sql
CREATE TABLE bookings (
    id VARCHAR(36) NOT NULL,
    customer_subject VARCHAR(255) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bookings_customer_subject ON bookings (customer_subject);
CREATE INDEX idx_bookings_event_id ON bookings (event_id);
CREATE INDEX idx_bookings_status ON bookings (status);
```

## REST API Specification (`/api/v1/bookings`)

### 1. Create Booking
**`POST /api/v1/bookings`** (Status `201 Created`)

Request Body:
```json
{
  "eventId": "e6a2b8e0-1b2c-4d3e-9f0a-1a2b3c4d5e6f",
  "quantity": 2,
  "amount": 998.00,
  "currency": "INR"
}
```

Response:
```json
{
  "id": "7fa12345-6789-4abc-def0-123456789abc",
  "customerSubject": "customer-default",
  "eventId": "e6a2b8e0-1b2c-4d3e-9f0a-1a2b3c4d5e6f",
  "quantity": 2,
  "amount": 998.00,
  "currency": "INR",
  "status": "PENDING_PAYMENT",
  "expiresAt": "2027-02-15T10:15:00Z",
  "createdAt": "2027-02-15T10:00:00Z",
  "updatedAt": "2027-02-15T10:00:00Z"
}
```

### 2. Get Booking by ID
**`GET /api/v1/bookings/{id}`** (Status `200 OK` or `404 Not Found`)

### 3. List My Bookings
**`GET /api/v1/bookings/me?page=0&size=20`** (Status `200 OK`)

### 4. Cancel Booking
**`POST /api/v1/bookings/{id}/cancel`** (Status `200 OK` or `409 Conflict`)

---

## Running and Testing

```powershell
# Run tests
.\mvnw.cmd test -pl services/booking-service

# Run application
.\mvnw.cmd -pl services/booking-service spring-boot:run
```
Service runs on port `8082`.
