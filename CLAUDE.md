# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
make build              # Compile and package (skip tests)
make run-dev            # Start Postgres + Redis in Docker, app locally with dev profile
make run                # Start everything via Docker Compose (prod profile)
make test               # Run all tests (requires Docker for Testcontainers)
make test-unit          # Run unit tests only (no Docker needed)
make test-integration   # Run integration tests only (requires Docker)
make coverage           # Run tests + generate JaCoCo report
make coverage-verify    # Enforce 80% line / 70% branch coverage thresholds
make load-test          # Run Gatling load tests (app must be running)
make security-scan      # OWASP dependency vulnerability check
```

Run a single test class:
```bash
./gradlew test --tests "com.movie.recommendation.modules.movie.MovieServiceTest"
```

Run a single test method:
```bash
./gradlew test --tests "com.movie.recommendation.modules.movie.MovieServiceTest.shouldCreateMovie"
```

### Frontend (Next.js 16 in `frontend/`)

```bash
cd frontend && npm run dev        # Dev server on :3000
cd frontend && npm run build      # Production build
cd frontend && npm run test:run   # Vitest tests
cd frontend && npm run lint       # ESLint
```

Or from root: `make frontend-dev`, `make frontend-build`, `make frontend-test`, `make frontend-lint`.

## Architecture

**Backend:** Java 21 / Spring Boot 3.3 / Gradle (Kotlin DSL)

The app follows a modular monolith structure under `src/main/java/com/movie/recommendation/`:

- **modules/** — Domain modules, each with Controller/Service/Repository/DTOs/Entities:
  - `auth` — JWT-based registration, login, token refresh, password reset
  - `movie` — Movie + Genre CRUD with soft-delete, full-text search, JPA Specifications for filtering
  - `review` — Reviews with likes, replies, ownership enforcement
  - `watchlist` — Per-user watchlist
  - `recommendation` — Strategy pattern engine with scheduled batch generation
  - `notification` — Persistent notifications + real-time WebSocket push via STOMP/SockJS
- **security/** — JWT filter chain, rate limiting (Bucket4j), request logging with traceId MDC
- **config/** — Spring Security config, Redis cache config (per-cache TTLs), WebSocket STOMP config, CORS, Swagger
- **exception/** — Global exception handler mapping domain exceptions to HTTP responses
- **common/** — Shared DTOs (`ApiResponse`, `PagedResponse`), constants, slug utility

### Key Patterns

- **Database migrations:** Flyway (`src/main/resources/db/migration/`). Schema changes go here, not JPA auto-DDL. JPA is set to `validate` mode.
- **Recommendation engine:** Strategy interface (`RecommendationStrategy`) with `CollaborativeFilteringStrategy` and `ContentBasedStrategy` implementations. `RecommendationScheduler` runs batch generation on a cron schedule.
- **Caching:** Redis with per-cache TTL configuration in `RedisConfig`. Cache keys prefixed with `movie-rec::`.
- **Notifications:** Event-driven via Spring's `@EventListener` in `NotificationEventListener`. Events (review liked, reply, new recommendations) create persistent notifications and push via WebSocket.
- **Auth flow:** Stateless JWT (HS512). Access token in Authorization header, refresh token in HTTP-only cookie. `JwtAuthenticationFilter` extracts and validates on every request.
- **Testing:** Unit tests use Mockito. Integration tests use Testcontainers (PostgreSQL + Redis) with full HTTP round-trips via `TestRestTemplate`.

### Frontend

Next.js 16 (App Router) with React 19, TypeScript, Tailwind CSS 4, Zustand for state, TanStack Query for server state, STOMP WebSocket for real-time notifications. Route groups: `(public)`, `(app)`, `(admin)`.

**Important:** The frontend uses Next.js 16 which has breaking changes from earlier versions. Always check `node_modules/next/dist/docs/` for current API documentation before writing Next.js code.

## Dev Profile Test Accounts

All passwords: `password`
- Admin: `admin@movie.com`
- Users: `alice@test.com`, `bob@test.com`, `charlie@test.com`, `diana@test.com`

## Environment

Requires `.env` file (copy from `.env.example`). Key vars: `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGINS`. See `application.yml` for all defaults.

API runs on `:8080`, Swagger UI at `/swagger-ui.html`, frontend on `:3000`.
