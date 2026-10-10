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
│   ├── gateway/                              # Milestone 3: Spring Cloud Gateway (Port 8080)
│   └── services/
│       ├── event-service/                    # Milestone 1: Event Service (Port 8081)
│       ├── booking-service/                  # Milestone 2: Booking Service (Port 8082)
│       ├── payment-service/                  # Milestone 5: Payment Service (Port 8083)
│       ├── notification-service/             # Milestone 6: Notification Service (Port 8084)
│       ├── analytics-service/                # Milestone 6: Analytics Service (Port 8085)
│       └── ai-service/                       # Milestone 7: AI Discovery & Support Service (Port 8086)
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
- [x] **Milestone 6 — Kafka Streams & RabbitMQ Notifications**: Completed (KRaft Apache Kafka domain event streams, Analytics Service on port 8085 with idempotent projection deduplication and aggregate metrics, RabbitMQ on port 8084 with dead-letter exchange/queue, email/SMS dispatching, and retry policies).
- [x] **Milestone 7 — Spring AI Event Discovery & Customer Support Assistant**: Completed (AI Discovery & Support Assistant on port 8086 powered by Groq LPU inference, gpt-oss-20b open-weights model, zero-hallucination real event grounding via Event Service, authoritative company policy & FAQ support for cancellations/refunds/holds/payments, and 5-layer Defense-in-Depth guardrails).
- [x] **Milestone 8 — Observability, Distributed Tracing & Master Docker Blueprint**: Completed (Micrometer Brave distributed tracing across all microservices with traceId/spanId MDC log correlation, Prometheus metrics scraping at `/actuator/prometheus`, master 15-container `docker-compose.yml` blueprint with Prometheus, Grafana, Zipkin, Keycloak, MySQL, Redis, Kafka, RabbitMQ, and lightweight Dockerfiles for all 7 microservices).
- [x] **Milestone 9 — Signed Ticket QR Generation, Gate Check-In Scanner & Email Delivery**: Completed (Cryptographically signed HMAC-SHA256 QR tokens `EP.<ticketId>.<bookingId>.<eventId>.<epoch>.<hex>`, Gate scanning endpoint `POST /api/v1/tickets/scan` restricted to ORGANIZER/ADMIN with anti-passback double-entry prevention and wrong event rejection, Branded HTML ticket email delivery via `notification-service`, and 15-min JWT session timeout with refresh token silent renewal).
- [x] **Milestone 10 — Performer Rosters, Seating Schemas & Reservations, Direct Google Calendar Sync**: Completed (Organizer performer/cast management with headshot URLs, bios, and roles; dedicated event poster support; custom organizer seating layout schemas; customer seat selection workflow with exact seat identifiers printed on tickets and embedded in signed QR passes; direct Google Calendar API integration via OAuth2 `https://www.googleapis.com/auth/calendar.events` scope; and universal RFC 5545 `.ics` export with calendar invite links in emails).
- [x] **Milestone 11 — Production DevOps, Executable Fat JAR Repackaging & Dual-Mode Orchestration**: Completed (Spring Boot maven plugin repackage goal configured across all microservices producing verified fat JARs; fully synchronized `.env` and `.env.example` configurations; root and backend `docker-compose.yml` for 15-container full stack orchestration; dedicated `scripts/` directory with `build-all`, `docker-start`, `docker-stop`, and `start-local-services`).


## Getting Started

### 1. Prerequisites
- **JDK 21+** (JDK 25 verified)
- **MySQL 8.0+** running on `localhost:3306` (or Docker)

### 2. Configure Backend Environment
Copy the environment template and adjust credentials if needed:
```powershell
copy backend\.env.example backend\.env
copy backend\.env.example .env
```

### 3. Build & Package Microservices
To package all microservices into executable Spring Boot fat JARs:
```powershell
.\scripts\build-all.ps1
# Or using Maven wrapper directly:
cd backend; .\mvnw.cmd clean package -DskipTests
```

### 4. Running the Full Stack (Dual Modes)

#### Option A: 15-Container Docker Compose Stack
```powershell
# Using the helper script:
.\scripts\docker-start.bat

# Or using standard Docker CLI:
docker compose up -d --build
```
This boots:
- MySQL 8.0 (Port 3306) & Redis (Port 6379)
- Keycloak 26 (Port 8180)
- Kafka KRaft (Port 9092) & RabbitMQ (Ports 5672, 15672)
- Prometheus (Port 9090), Grafana (Port 3000), Zipkin (Port 9411)
- All 7 EventPulse microservices behind API Gateway (Port 8080)

#### Option B: Local Standalone Execution (No Docker required)
View status and execute individual fat JARs:
```powershell
.\scripts\start-local-services.ps1

# Run individual services:
java -jar backend\services\event-service\target\event-service-0.0.1-SNAPSHOT.jar
java -jar backend\services\booking-service\target\booking-service-0.0.1-SNAPSHOT.jar
java -jar backend\gateway\target\gateway-0.0.1-SNAPSHOT.jar
```


