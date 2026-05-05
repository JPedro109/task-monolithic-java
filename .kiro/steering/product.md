# Task Service

A RESTful API for personal task management built with Java 21 and Spring Boot 4, following Clean Architecture principles.

## What it does

- Users register and authenticate via JWT (access + refresh tokens)
- Each authenticated user can create, list, update, delete, and mark their own tasks as finished
- Tasks are strictly user-scoped — users can only access their own data

## API surface

| Domain | Base path |
|--------|-----------|
| Auth   | `/api/v1/auth` |
| Users  | `/api/v1/users` |
| Tasks  | `/api/v1/tasks` |

Swagger UI is available at `http://localhost:8080/swagger-ui.html` when the app is running.

## Observability

The service emits metrics and traces via OpenTelemetry. A full Docker Compose stack spins up PostgreSQL, Prometheus, Grafana (with a pre-provisioned dashboard), and an OTEL Collector alongside the app.
