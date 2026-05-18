# Movie Recommendation System

A RESTful API backend for a movie recommendation platform built with Java 21, Spring Boot 3.3, PostgreSQL, and Redis. Secured with stateless JWT authentication and role-based access control.

## Tech Stack

| Layer           | Technology                                       |
|-----------------|--------------------------------------------------|
| Language        | Java 21 LTS                                      |
| Framework       | Spring Boot 3.3.5                                |
| Database        | PostgreSQL 16                                    |
| Cache           | Redis 7                                          |
| Authentication  | JWT (HS512) via JJWT 0.12.6                      |
| Build Tool      | Gradle 8.7 (Kotlin DSL)                          |
| API Docs        | SpringDoc OpenAPI 2.6.0 (Swagger UI)             |
| Testing         | JUnit 5, Mockito, Testcontainers, AssertJ        |

---

## Getting Started

### Prerequisites

- **Java 21** (JDK)
- **Docker** and **Docker Compose**
- **Make**

### Quick Start

```bash
cp .env.example .env      # configure DB_PASSWORD, JWT_SECRET, etc.
make run                   # builds and starts app + Postgres + Redis
```

The API is available at `http://localhost:8080` and Swagger UI at `http://localhost:8080/swagger-ui.html`.

### Local Development

```bash
make run-dev               # starts Postgres + Redis in Docker, app on host with dev profile
```

### All Make Commands

Run `make help` to see the full list:

| Command              | Description                                      |
|----------------------|--------------------------------------------------|
| `make build`         | Compile and package (skip tests)                 |
| `make run`           | Start app + Postgres + Redis via Docker Compose  |
| `make run-dev`       | Start infra in Docker, app locally (dev profile) |
| `make test`          | Run all tests (requires Docker)                  |
| `make test-unit`     | Run unit tests only (no Docker needed)           |
| `make test-integration` | Run integration tests only (requires Docker)  |
| `make docker-up`     | Start all containers in background               |
| `make docker-down`   | Stop and remove containers                       |
| `make docker-rebuild`| Rebuild from scratch (remove volumes)            |
| `make infra`         | Start only Postgres + Redis                      |
| `make logs`          | Tail backend container logs                      |
| `make clean`         | Remove build artifacts                           |

### Auth Endpoints

| Method | Endpoint                        | Description                         |
|--------|---------------------------------|-------------------------------------|
| POST   | `/api/v1/auth/register`         | Register a new user account         |
| POST   | `/api/v1/auth/login`            | Authenticate and receive tokens     |
| POST   | `/api/v1/auth/refresh`          | Refresh access token (cookie-based) |
| POST   | `/api/v1/auth/logout`           | Invalidate refresh token            |
| POST   | `/api/v1/auth/forgot-password`  | Request a password reset token      |
| POST   | `/api/v1/auth/reset-password`   | Reset password using reset token    |

### Environment Variables

| Variable             | Default                            | Description                                |
|----------------------|------------------------------------|--------------------------------------------|
| `DB_HOST`            | `localhost`                        | PostgreSQL host                            |
| `DB_PORT`            | `5432`                             | PostgreSQL port                            |
| `DB_NAME`            | `movie_rec`                        | Database name                              |
| `DB_USER`            | `postgres`                         | Database username                          |
| `DB_PASSWORD`        | `postgres`                         | Database password                          |
| `REDIS_HOST`         | `localhost`                        | Redis host                                 |
| `REDIS_PORT`         | `6379`                             | Redis port                                 |
| `JWT_SECRET`         | *(dev default in application.yml)* | Base64-encoded HS512 secret (min 64 bytes) |
| `JWT_ACCESS_EXPIRY`  | `900000`                           | Access token lifetime in ms (15 min)       |
| `JWT_REFRESH_EXPIRY` | `604800000`                        | Refresh token lifetime in ms (7 days)      |
| `CORS_ORIGINS`       | `http://localhost:3000`            | Allowed CORS origins                       |

---

## Project Development Phases

| Phase | Milestone                     | Duration  | Status        |
|-------|-------------------------------|-----------|---------------|
| 1     | Foundation & Auth             | Week 1-2  | **Completed** |
| 2     | Core CRUD                     | Week 3-4  | Planned       |
| 3     | Caching & Search              | Week 5    | Planned       |
| 4     | Recommendation Engine         | Week 6-7  | Planned       |
| 5     | Notifications & WebSocket     | Week 8    | Planned       |
| 6     | Testing & Hardening           | Week 9-10 | Planned       |
| 7     | Docker & Documentation        | Week 11   | Planned       |

> Full technical details for each phase are in [`docs/proposal.md`](docs/proposal.md).

---

## Testing Guide

| Layer       | Command                | Docker Required | Description                                     |
|-------------|------------------------|-----------------|-------------------------------------------------|
| All         | `make test`            | Yes             | Unit + integration tests                        |
| Unit        | `make test-unit`       | No              | Service logic, JWT operations (Mockito)         |
| Integration | `make test-integration`| Yes             | Full HTTP with Testcontainers (Postgres + Redis)|

### Current Test Coverage

| Test Class                       | Tests | Covers                                            |
|----------------------------------|-------|---------------------------------------------------|
| `JwtTokenProviderTest`           | 6     | Token generation, validation, expiry, malformed   |
| `AuthServiceTest`                | 4     | Register/login success, duplicate email/username  |
| `AuthIntegrationTest`            | 5     | Full HTTP: register, login, duplicate, 401 flows  |
| `RecommendationApplicationTests` | 1     | Spring context loads with Testcontainers          |

Integration tests use [Testcontainers](https://www.testcontainers.org/) to spin up disposable PostgreSQL and Redis instances automatically. Docker must be running before executing `make test` or `make test-integration`.
