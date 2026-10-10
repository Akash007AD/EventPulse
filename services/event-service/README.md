# EventPulse — Event Service

Event Service is the core catalogue and lifecycle microservice of the EventPulse platform. It is responsible for storing, updating, searching, and managing event schedules, venues, and publication statuses.

## Tech Stack & Architecture

- **Language & Runtime:** Java 21 LTS
- **Framework:** Spring Boot 3.4.3
- **Boilerplate Reduction:** Project Lombok (@Getter, @Setter, @Builder, @RequiredArgsConstructor, @Slf4j)
- **Data Persistence:** Spring Data JPA / Hibernate 6 with MySQL Dialect
- **Database:** MySQL 8.0+
- **Database Migrations:** Flyway (`flyway-mysql`)
- **Validation:** Jakarta Bean Validation (`@ValidDateRange`, `@NotBlank`, etc.)
- **Error Handling:** RFC 7807 `ProblemDetail`
- **Operational Monitoring:** Spring Boot Actuator (`/actuator/health`, `/actuator/info`, `/actuator/metrics`)
- **Testing:** JUnit 5, Mockito, Spring Boot Test, H2 (MySQL mode for tests), Testcontainers MySQL

## Database Schema (MySQL)

Schema migration is managed exclusively through Flyway (`src/main/resources/db/migration/V1__create_events_table.sql`):

```sql
CREATE TABLE events (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    venue_name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    starts_at DATETIME(6) NOT NULL,
    ends_at DATETIME(6) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    organizer_subject VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_events_status_city ON events (status, city);
CREATE INDEX idx_events_starts_at ON events (starts_at);
```

## Local Configuration & Environment Variables

Configure the following environment variables or use the defaults in `application.yml`:

| Variable | Default Value | Description |
|---|---|---|
| `EVENT_SERVICE_PORT` | `8081` | HTTP port for Event Service |
| `EVENT_DB_HOST` | `localhost` | MySQL host |
| `EVENT_DB_PORT` | `3306` | MySQL port |
| `EVENT_DB_NAME` | `eventpulse_event` | Database name |
| `EVENT_DB_USERNAME` | `root` | Database username |
| `EVENT_DB_PASSWORD` | `root` | Database password |

### Preparing the Local MySQL Database

Connect to your MySQL server and run:

```sql
CREATE DATABASE IF NOT EXISTS eventpulse_event CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

*(Note: The JDBC URL also includes `createDatabaseIfNotExist=true` for automatic creation if your MySQL user has sufficient privileges).*

## Building and Running

### Run automated tests:
```powershell
# From the repository root
.\mvnw.cmd clean test
```

### Start the application locally:
```powershell
# From repository root
.\mvnw.cmd -pl services/event-service spring-boot:run

# Or with custom DB credentials:
$env:EVENT_DB_USERNAME="root"
$env:EVENT_DB_PASSWORD="your_password"
.\mvnw.cmd -pl services/event-service spring-boot:run
```

## REST API Specification (`/api/v1/events`)

### 1. Create Event
**`POST /api/v1/events`** (Status `201 Created`)

Request Body:
```json
{
  "name": "Kolkata Developer Meetup",
  "description": "Talks and networking for software developers",
  "venueName": "Biswa Bangla Convention Centre",
  "city": "Kolkata",
  "startsAt": "2027-02-15T10:00:00Z",
  "endsAt": "2027-02-15T17:00:00Z",
  "currency": "INR",
  "status": "DRAFT",
  "organizerSubject": "org-user-42"
}
```

Response:
```json
{
  "id": "e6a2b8e0-1b2c-4d3e-9f0a-1a2b3c4d5e6f",
  "name": "Kolkata Developer Meetup",
  "description": "Talks and networking for software developers",
  "venueName": "Biswa Bangla Convention Centre",
  "city": "Kolkata",
  "startsAt": "2027-02-15T10:00:00Z",
  "endsAt": "2027-02-15T17:00:00Z",
  "currency": "INR",
  "status": "DRAFT",
  "organizerSubject": "org-user-42",
  "createdAt": "2026-10-10T11:00:00Z",
  "updatedAt": "2026-10-10T11:00:00Z"
}
```

### 2. Search / List Published Events
**`GET /api/v1/events?page=0&size=20&city=Kolkata`** (Status `200 OK`)

Response:
```json
{
  "content": [
    {
      "id": "e6a2b8e0-1b2c-4d3e-9f0a-1a2b3c4d5e6f",
      "name": "Kolkata Developer Meetup",
      "status": "PUBLISHED"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### 3. Get Event by ID
**`GET /api/v1/events/{id}`** (Status `200 OK` or `404 Not Found`)

### 4. Update Event
**`PUT /api/v1/events/{id}`** (Status `200 OK` or `404 Not Found`, `409 Conflict`)

### 5. Archive Event (Soft-Deactivation)
**`DELETE /api/v1/events/{id}`** (Status `200 OK`)
Sets `status = ARCHIVED` without physically deleting the database row.

## Known Limitations

- **Authentication / Authorization:** In this initial local Milestone 1, endpoints are unauthenticated. Keycloak OIDC/JWT role enforcement (`ORGANIZER`, `CUSTOMER`, `ADMIN`) and event ownership checks will be integrated in Milestone 3.
