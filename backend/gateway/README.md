# EventPulse — API Gateway

Edge API Gateway built with Spring Cloud Gateway (reactive Netty) on Java 21 LTS and Spring Boot 3.4.3.

## Responsibilities

- **Unified Edge Entry Point**: Port `8080`.
- **Dynamic Routing**:
  - `/api/v1/events/**` -> Event Service (`http://localhost:8081`)
  - `/api/v1/bookings/**` -> Booking Service (`http://localhost:8082`)
- **Central CORS Configuration**: Pre-configured for web frontends (`http://localhost:3000`, `http://localhost:5173`).
- **Actuator Health & Observability**: Health and route inspection endpoints exposed under `/actuator`.

## Configuration

| Environment Variable | Default Value | Description |
|---|---|---|
| `GATEWAY_PORT` | `8080` | Port the Gateway listens on |
| `EVENT_SERVICE_URL` | `http://localhost:8081` | Downstream URL for Event Service |
| `BOOKING_SERVICE_URL` | `http://localhost:8082` | Downstream URL for Booking Service |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Comma-separated allowed frontend origins |

## Running Locally

```powershell
cd backend
.\mvnw.cmd -pl gateway spring-boot:run
```

Health check: `http://localhost:8080/actuator/health`
