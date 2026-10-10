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
├── pom.xml                                   # Root aggregator POM
├── mvnw / mvnw.cmd                           # Maven Wrapper
├── .gitignore
├── .env.example                              # Environment configuration template
├── docs/                                     # Comprehensive architecture & design docs
│   ├── 00_PROJECT_BRIEF.md
│   ├── 01_ARCHITECTURE.md
│   ├── 02_TECHNOLOGY_DECISIONS.md
│   ├── 03_SERVICE_BOUNDARIES.md
│   ├── 04_REPOSITORY_STRUCTURE.md
│   ├── 05_API_CONTRACTS.md
│   ├── 06_DATA_MODEL.md
│   ├── 07_BOOKING_AND_MESSAGING_WORKFLOWS.md
│   ├── 08_SECURITY.md
│   ├── 09_AI_FEATURES.md
│   ├── 10_TESTING_AND_OBSERVABILITY.md
│   ├── 11_IMPLEMENTATION_ROADMAP.md
│   ├── 12_ANTIGRAVITY_WORKING_RULES.md
│   ├── 13_MILESTONE_1_EVENT_SERVICE.md
│   └── 14_DEFINITION_OF_DONE_AND_REVIEW.md
└── services/
    └── event-service/                        # Milestone 1: Event Service
        ├── pom.xml
        ├── src/
        └── README.md
```

## Milestone Status

- [x] **Milestone 1 — Event Service**: Completed (MySQL 8 persistence, Flyway migrations, full CRUD/search API, validation, RFC 7807 errors, unit & integration tests).
- [x] **Milestone 2 — Booking Service**: Completed (MySQL 8 persistence, state machine, Event Service HTTP integration, hold windows, unit & integration tests).
- [ ] **Milestone 3 — Spring Cloud Gateway & Keycloak**: Pending.
- [ ] **Milestone 4 — Redis Caching & Distributed Holds**: Pending.
- [ ] **Milestone 5 — Payment Service & Transactional Outbox**: Pending.
- [ ] **Milestone 6 — Kafka Streams & RabbitMQ Notifications**: Pending.
- [ ] **Milestone 7 — Spring AI Event Discovery**: Pending.
- [ ] **Milestone 8 — Observability, End-to-End Resilience & Docker Compose**: Pending.

## Getting Started

### 1. Prerequisites
- **JDK 21+** (JDK 25 LTS verified)
- **MySQL 8.0+** running on `localhost:3306`

### 2. Configure Environment
Copy `.env.example` to `.env` or set environment variables:
```powershell
$env:EVENT_DB_USERNAME="root"
$env:EVENT_DB_PASSWORD="your_password"
```

### 3. Run Build & Tests
```powershell
.\mvnw.cmd clean test
```

### 4. Run Event Service
```powershell
.\mvnw.cmd -pl services/event-service spring-boot:run
```
Service runs on `http://localhost:8081`. Health check available at `http://localhost:8081/actuator/health`.
