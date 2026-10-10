# EventPulse — Distributed Event Booking & Intelligent Ticketing Platform

EventPulse is a production-oriented microservices platform for event discovery, ticket reservations, real payments with Razorpay, asynchronous notifications, event analytics, and natural-language AI discovery.

## Architecture & Technology Stack

- **Framework:** Spring Boot 3.4.3 on Java 21 LTS
- **Boilerplate Reduction:** Project Lombok
- **Build System:** Maven Multi-Module with Maven Wrapper (`mvnw` / `mvnw.cmd`)
- **Primary Database:** MySQL 8.0+ (per-service databases: `eventpulse_event`, `eventpulse_booking`, etc.)
- **Persistence & Migrations:** Spring Data JPA, Hibernate 6, Flyway (`flyway-mysql`)
- **API & Error Handling:** Spring Web, Bean Validation, RFC 7807 `ProblemDetail`
- **Cache & Concurrency:** Redis (Milestone 4)
- **Identity & Edge Routing:** Keycloak OIDC/JWT + Spring Cloud Gateway (Milestone 3)
- **Messaging:** Apache Kafka (Business Event Stream) & RabbitMQ (Notification Queue) (Milestone 6)
- **AI Discovery:** Spring AI with tool-calling (Milestone 7)

## Monorepo Layout

```text
EventPulse/
├── backend/                                  # Spring Boot microservices backend
│   ├── pom.xml                               # Backend aggregator POM
│   ├── mvnw / mvnw.cmd                       # Maven Wrapper
│   ├── .env.example                          # Environment configuration template
│   └── services/
│       ├── event-service/                    # Milestone 1: Event Service (Port 8081)
│       └── booking-service/                  # Milestone 2: Booking Service (Port 8082)
├── frontend/                                 # Web frontend application (upcoming)
├── docs/                                     # Architecture & design documentation
└── .gitignore
```

## Milestone Status

- [x] **Milestone 1 — Event Service**: Completed (MySQL 8 persistence, Flyway migrations, full CRUD/search API, validation, RFC 7807 errors, unit & integration tests).
- [x] **Milestone 2 — Booking Service**: Completed (MySQL 8 persistence, state machine, Event Service HTTP integration, hold windows, unit & integration tests).
- [x] **Milestone 3 — Spring Cloud Gateway & Keycloak Identity**: Completed (Spring Cloud Gateway on port 8080, Keycloak OAuth2 Resource Server JWT validation, fine-grained RBAC with CUSTOMER/ORGANIZER/ADMIN roles, Google social identity brokering, and Organizer Onboarding flow).
- [x] **Milestone 4 — Redis Caching & Distributed Holds**: Completed (Event Service Redis catalog caching with 10-min TTL, Redisson distributed concurrency locks, temporary ticket holds with automated TTL recovery, and HTTP 429 backpressure).
- [x] **Milestone 5 — Payment Service & Transactional Outbox (Razorpay)**: Completed (Payment Service on port 8083, Razorpay Orders API, HMAC-SHA256 signature verification, Idempotency-Key support, Transactional Outbox pattern, Webhook reconciler, and refunds).
- [ ] **Milestone 6 — Kafka Streams & RabbitMQ Notifications**: Pending.
- [ ] **Milestone 7 — Spring AI Event Discovery**: Pending.
- [ ] **Milestone 8 — Observability, End-to-End Resilience & Docker Compose**: Pending.

## Getting Started

### 1. Prerequisites
- **JDK 21+** (JDK 25 verified)
- **MySQL 8.0+** running on `localhost:3306`

### 2. Configure Backend Environment
Navigate to `backend/` and configure environment variables or create `backend/.env` from `backend/.env.example`:
```powershell
cd backend
copy .env.example .env
```
Ensure your database credentials and connection URLs are configured.

### 3. Run Build & Tests
```powershell
cd backend
.\mvnw.cmd clean test
```

### 4. Run Services Locally
```powershell
cd backend
# Run Event Service (Port 8081)
.\mvnw.cmd -pl services/event-service spring-boot:run

# Run Booking Service (Port 8082)
.\mvnw.cmd -pl services/booking-service spring-boot:run
```

