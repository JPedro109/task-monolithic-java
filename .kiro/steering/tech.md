# Tech Stack

## Core

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| Database | PostgreSQL 15 |
| Migrations | Flyway (versioned scripts in `src/main/resources/db/migration/`) |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security + JWT via JJWT 0.12.6 |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Gradle 8 with Kotlin DSL (`build.gradle.kts`) |

## Observability

- OpenTelemetry (Spring Boot starter)
- Micrometer + OTLP registry
- Spring Boot Actuator (exposes `health`, `info`, `metrics`)
- Prometheus + Grafana via Docker Compose

## Testing

| Tool | Purpose |
|------|---------|
| JUnit 5 | Unit and integration tests |
| Mockito | Mocking in unit tests (`@ExtendWith(MockitoExtension.class)`) |
| Testcontainers (PostgreSQL) | Real DB for integration tests |
| Spring MockMvc | HTTP-layer integration tests |
| JaCoCo | Code coverage (minimum 85%, enforced at build time) |
| Checkstyle 10.21.4 | Static analysis (zero warnings allowed) |

## Common Commands

```bash
# Run the app (requires PostgreSQL running or Docker stack up)
./gradlew bootRun

# Run all tests (requires Docker for Testcontainers)
./gradlew test

# Run tests + coverage check + Checkstyle
./gradlew check

# Start infrastructure (PostgreSQL, Prometheus, Grafana, OTEL Collector)
cd docker && docker compose up -d

# View coverage report (after running tests)
# build/reports/jacoco/test/html/index.html
```

## Key Configuration

All runtime config is in `src/main/resources/application.yaml` and driven by environment variables:

| Variable | Default | Notes |
|----------|---------|-------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `5432` / `task` | PostgreSQL connection |
| `DB_USER` / `DB_PASSWORD` | `postgres` / `postgres` | |
| `JWT_SECRET` | `change-me-...` | Must be ≥ 32 chars; always override in production |
| `JWT_ACCESS_EXPIRATION_MS` | `900000` (15 min) | |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` (7 days) | |
| `ENABLE_OTLP_COLLECTOR` | `false` | Set to `true` to push metrics |
| `OTLP_COLLECTOR_URL` | — | Required when OTLP is enabled |

Integration tests use the `integration-test` Spring profile (`src/test/resources/application-integration-test.yaml`) and spin up a Testcontainers PostgreSQL instance automatically.
