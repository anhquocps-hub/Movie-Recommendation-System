# Movie Recommendation System

A RESTful API backend for a movie recommendation platform built with Java 21, Spring Boot 3.3, PostgreSQL, and Redis. Secured with stateless JWT authentication and role-based access control.

## Tech Stack

| Layer           | Technology                                        |
|-----------------|---------------------------------------------------|
| Language        | Java 21 LTS                                       |
| Framework       | Spring Boot 3.3.5                                 |
| Database        | PostgreSQL 16                                     |
| Cache           | Redis 7                                           |
| Authentication  | JWT (HS512) via JJWT 0.12.6                       |
| Build Tool      | Gradle 8.7 (Kotlin DSL)                           |
| API Docs        | SpringDoc OpenAPI 2.6.0 (Swagger UI)              |
| Monitoring      | Spring Boot Actuator, Micrometer, Prometheus      |
| Testing         | JUnit 5, Mockito, Testcontainers, AssertJ, JaCoCo |
| Load Testing    | Gatling 3.10                                      |
| Security        | Bucket4j rate limiting, OWASP Dependency-Check    |

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

| Command                 | Description                                      |
|-------------------------|--------------------------------------------------|
| `make build`            | Compile and package (skip tests)                 |
| `make run`              | Start app + Postgres + Redis via Docker Compose  |
| `make run-dev`          | Start infra in Docker, app locally (dev profile) |
| `make test`             | Run all tests (requires Docker)                  |
| `make test-unit`        | Run unit tests only (no Docker needed)           |
| `make test-integration` | Run integration tests only (requires Docker)     |
| `make coverage`         | Run tests and generate JaCoCo coverage report    |
| `make coverage-verify`  | Verify coverage meets thresholds (80%/70%)       |
| `make load-test`        | Run Gatling load tests (requires running app)    |
| `make security-scan`    | Run OWASP dependency vulnerability check         |
| `make docker-up`        | Start all containers in background               |
| `make docker-down`      | Stop and remove containers                       |
| `make docker-rebuild`   | Rebuild from scratch (remove volumes)            |
| `make infra`            | Start only Postgres + Redis                      |
| `make logs`             | Tail backend container logs                      |
| `make clean`            | Remove build artifacts                           |

### API Endpoints

#### Auth

| Method | Endpoint                        | Access  | Description                         |
|--------|---------------------------------|---------|-------------------------------------|
| POST   | `/api/v1/auth/register`         | Public  | Register a new user account         |
| POST   | `/api/v1/auth/login`            | Public  | Authenticate and receive tokens     |
| POST   | `/api/v1/auth/refresh`          | Public  | Refresh access token (cookie-based) |
| POST   | `/api/v1/auth/logout`           | USER+   | Invalidate refresh token            |
| POST   | `/api/v1/auth/forgot-password`  | Public  | Request a password reset token      |
| POST   | `/api/v1/auth/reset-password`   | Public  | Reset password using reset token    |

#### Movies & Genres

| Method | Endpoint                      | Access  | Description                         |
|--------|-------------------------------|---------|-------------------------------------|
| GET    | `/api/v1/movies`              | Public  | List movies (filter by genre, year, rating) |
| GET    | `/api/v1/movies/{id}`         | Public  | Get movie details                   |
| GET    | `/api/v1/movies/search`       | Public  | Full-text search (optional genre filter) |
| GET    | `/api/v1/movies/trending`     | Public  | Get trending movies (cached)        |
| POST   | `/api/v1/movies`              | ADMIN   | Create a new movie                  |
| PUT    | `/api/v1/movies/{id}`         | ADMIN   | Update movie details                |
| DELETE | `/api/v1/movies/{id}`         | ADMIN   | Soft-delete a movie                 |
| PATCH  | `/api/v1/movies/{id}/restore` | ADMIN   | Restore a soft-deleted movie        |
| GET    | `/api/v1/genres`              | Public  | List all genres (cached)            |
| POST   | `/api/v1/genres`              | ADMIN   | Create a genre                      |
| PUT    | `/api/v1/genres/{id}`         | ADMIN   | Update a genre                      |
| DELETE | `/api/v1/genres/{id}`         | ADMIN   | Delete a genre                      |

#### Reviews

| Method | Endpoint                              | Access      | Description                          |
|--------|---------------------------------------|-------------|--------------------------------------|
| GET    | `/api/v1/movies/{movieId}/reviews`    | Public      | List reviews for a movie (paginated) |
| POST   | `/api/v1/movies/{movieId}/reviews`    | USER+       | Submit a review (1 per user/movie)   |
| PUT    | `/api/v1/reviews/{id}`                | Owner       | Edit own review                      |
| DELETE | `/api/v1/reviews/{id}`                | Owner/ADMIN | Delete a review                      |
| POST   | `/api/v1/reviews/{id}/like`           | USER+       | Toggle like on a review              |
| POST   | `/api/v1/reviews/{id}/replies`        | USER+       | Reply to a review                    |
| GET    | `/api/v1/reviews/{id}/replies`        | Public      | List replies for a review            |

#### Watchlist

| Method | Endpoint                      | Access  | Description                     |
|--------|-------------------------------|---------|---------------------------------|
| GET    | `/api/v1/watchlist`           | USER+   | Get user's watchlist (paginated)|
| POST   | `/api/v1/watchlist/{movieId}` | USER+   | Add movie to watchlist          |
| DELETE | `/api/v1/watchlist/{movieId}` | USER+   | Remove movie from watchlist     |

#### Users

| Method | Endpoint                       | Access  | Description                      |
|--------|--------------------------------|---------|----------------------------------|
| GET    | `/api/v1/users/me`             | USER+   | Get current user's profile       |
| PUT    | `/api/v1/users/me`             | USER+   | Update profile (username, avatar)|
| PUT    | `/api/v1/users/me/preferences` | USER+   | Update genre preferences         |
| GET    | `/api/v1/users`                | ADMIN   | List all users (paginated)       |
| GET    | `/api/v1/users/{id}`           | ADMIN   | Get any user's profile           |
| PATCH  | `/api/v1/users/{id}/role`      | ADMIN   | Change a user's role             |
| DELETE | `/api/v1/users/{id}`           | ADMIN   | Deactivate a user account        |

#### Recommendations

| Method | Endpoint                          | Access  | Description                              |
|--------|-----------------------------------|---------|------------------------------------------|
| GET    | `/api/v1/recommendations`         | USER+   | Get personalized recommendations (paged) |
| POST   | `/api/v1/recommendations/refresh` | ADMIN   | Manually trigger recommendation job      |

#### Notifications

| Method | Endpoint                          | Access  | Description                              |
|--------|-----------------------------------|---------|------------------------------------------|
| GET    | `/api/v1/notifications`           | USER+   | List user's notifications (paginated)    |
| PATCH  | `/api/v1/notifications/{id}/read` | USER+   | Mark a notification as read              |
| PATCH  | `/api/v1/notifications/read-all`  | USER+   | Mark all notifications as read           |
| GET    | `/api/v1/notifications/unread-count` | USER+ | Get count of unread notifications        |
| WS     | `/ws` (STOMP/SockJS)              | USER+   | WebSocket endpoint for real-time updates |

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
| 2     | Core CRUD                     | Week 3-4  | **Completed** |
| 3     | Caching & Search              | Week 5    | **Completed** |
| 4     | Recommendation Engine         | Week 6-7  | **Completed** |
| 5     | Notifications & WebSocket     | Week 8    | **Completed** |
| 6     | Testing & Hardening           | Week 9-10 | **Completed** |
| 7     | Docker & Documentation        | Week 11   | Planned       |

> Full technical details for each phase are in [`docs/proposal.md`](docs/proposal.md).

---

## Testing Guide

| Layer       | Command                | Docker Required | Description                                     |
|-------------|------------------------|-----------------|-------------------------------------------------|
| All         | `make test`            | Yes             | Unit + integration tests                        |
| Unit        | `make test-unit`       | No              | Service logic, JWT operations (Mockito)         |
| Integration | `make test-integration`| Yes             | Full HTTP with Testcontainers (Postgres + Redis)|

### Current Test Coverage (144 tests)

**Unit Tests (Mockito)**

| Test Class                    | Tests | Covers                                                     |
|-------------------------------|-------|------------------------------------------------------------|
| `JwtTokenProviderTest`        | 7     | Token generation, validation, expiry, malformed            |
| `AuthServiceTest`             | 4     | Register/login success, duplicate email/username           |
| `GenreServiceTest`            | 6     | CRUD, duplicate name detection                             |
| `MovieServiceTest`            | 10    | CRUD, soft delete/restore, search, slug collision          |
| `ReviewServiceTest`           | 14    | CRUD, ownership, like toggle, reply, admin bypass, events  |
| `WatchlistServiceTest`        | 5     | Add/remove, duplicate detection, not-found                 |
| `UserServiceTest`             | 7     | Profile, preferences, role change, deactivate, invalid role|
| `RecommendationServiceTest`   | 10    | Strategy selection, pagination, active users, cache evict  |
| `NotificationServiceTest`     | 6     | CRUD, unread count, WebSocket push, not-found              |
| `NotificationEventListenerTest` | 3   | Event handling for like, reply, recommendation             |
| `RateLimitFilterTest`         | 5     | Allow/block requests, X-Forwarded-For, per-IP buckets, 429 |
| `RequestLoggingFilterTest`    | 7     | TraceId MDC, X-Trace-Id header, MDC cleanup, path skipping |
| `CustomHealthIndicatorTest`   | 5     | DB up/down, Redis up/down, both down, invalid connection    |
| `RedisConfigTest`             | 1     | Per-cache TTL configurations                               |

**Integration Tests (Testcontainers)**

| Test Class                          | Tests | Covers                                            |
|-------------------------------------|-------|---------------------------------------------------|
| `AuthIntegrationTest`               | 5     | Register, login, duplicate, 401 flows             |
| `MovieIntegrationTest`              | 8     | Genre/movie CRUD, public access, admin vs user    |
| `ReviewIntegrationTest`             | 6     | Review lifecycle, duplicate 409, like, reply      |
| `WatchlistIntegrationTest`          | 4     | Add/get/remove, duplicate 409                     |
| `RecommendationIntegrationTest`     | 8     | Get recommendations, refresh job, auth, pagination|
| `NotificationIntegrationTest`       | 7     | Notification REST API, event-driven creation, auth|
| `SecurityHardeningIntegrationTest`  | 9     | Actuator endpoints, security headers, trace ID    |
| `RecommendationApplicationTests`    | 1     | Spring context loads with Testcontainers          |

Integration tests use [Testcontainers](https://www.testcontainers.org/) to spin up disposable PostgreSQL and Redis instances automatically. Docker must be running before executing `make test` or `make test-integration`.

### Code Coverage (JaCoCo)

```bash
make coverage           # Run tests and generate report
make coverage-verify    # Enforce 80% line / 70% branch coverage
```

Reports are generated at `build/reports/jacoco/test/html/index.html`.

---

## Health & Monitoring

Spring Boot Actuator endpoints are available for health checks and metrics:

| Endpoint                    | Access  | Description                    |
|-----------------------------|---------|--------------------------------|
| `/actuator/health`          | Public  | Application health status      |
| `/actuator/health/liveness` | Public  | Kubernetes liveness probe      |
| `/actuator/health/readiness`| Public  | Kubernetes readiness probe     |
| `/actuator/info`            | Public  | Application info               |
| `/actuator/metrics`         | AUTH    | Application metrics            |
| `/actuator/prometheus`      | AUTH    | Prometheus-format metrics      |

Custom health indicators check PostgreSQL and Redis connectivity.

---

## Security Hardening

### Security Headers

All responses include:
- **Strict-Transport-Security**: `max-age=31536000; includeSubDomains`
- **Content-Security-Policy**: `default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'`
- **X-Frame-Options**: `DENY`
- **X-Content-Type-Options**: `nosniff`

### Rate Limiting

IP-based rate limiting via Bucket4j: **100 requests per minute** per client IP. Exceeding the limit returns `429 Too Many Requests`.

### Request Size Limits

- Max file upload: 10 MB
- Max request size: 10 MB
- Max HTTP header size: 20 KB

---

## Structured Logging

Logging is profile-aware:

| Profile   | Format        | Details                              |
|-----------|---------------|--------------------------------------|
| `dev`     | Human-readable| Timestamp, thread, level, message    |
| `test`    | Human-readable| Same as dev                          |
| `prod`    | JSON (Logstash)| Structured with `traceId`, `userId` |

Each request gets a unique `traceId` (returned in `X-Trace-Id` response header) and authenticated requests include `userId` in MDC context.

---

## Load Testing

Gatling load test simulation included at `src/gatling/java/MovieApiSimulation.java`.

```bash
make run-dev            # Start the application first
make load-test          # Run Gatling simulation
```

The simulation covers:
- **Browse Movies**: Public endpoints (list, search, detail, genres)
- **Authenticated Flow**: Login, recommendations, watchlist, notifications
- **Health Check**: Actuator health endpoint

Assertions: p95 response time < 2s, >95% success rate. Reports at `build/reports/gatling/`.

---

## Security Scanning

OWASP Dependency-Check scans all dependencies for known vulnerabilities.

```bash
make security-scan      # Run vulnerability scan
```

Builds fail if any dependency has a CVSS score >= 7.0. False positives can be suppressed in `owasp-suppressions.xml`. Report at `build/reports/dependency-check-report.html`.

---

## Postman Collection

A complete Postman collection is available in the `postman/` directory:

1. Import `postman/Movie-Recommendation-API.postman_collection.json`
2. Import `postman/Movie-Recommendation-API.postman_environment.json`
3. Start the app with `make run-dev`
4. Run **Auth > Login (User)** to auto-set the `accessToken` variable
5. Run **Auth > Login (Admin)** for admin-only endpoints

The collection includes test scripts that validate response codes, set environment variables, and chain requests together.

**Dev profile test accounts** (all passwords: `password`):
- Admin: `admin@movie.com`
- Users: `alice@test.com`, `bob@test.com`, `charlie@test.com`, `diana@test.com`
