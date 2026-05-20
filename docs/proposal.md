# Backend Architecture Proposal — Movie Recommendation System

**Stack**       | Java 21 · Spring Boot 3.x · PostgreSQL 16 · Redis 7 · Docker 

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [System Architecture](#2-system-architecture)
3. [Technology Stack & Justification](#3-technology-stack--justification)
4. [Project Structure](#4-project-structure)
5. [Database Schema Design (PostgreSQL)](#5-database-schema-design-postgresql)
6. [API Design Standards & Conventions](#6-api-design-standards--conventions)
7. [Authentication & Authorization (JWT + RBAC)](#7-authentication--authorization-jwt--rbac)
8. [Caching Strategy (Redis)](#8-caching-strategy-redis)
9. [Recommendation Engine](#9-recommendation-engine)
10. [Real-Time Notification System (WebSocket)](#10-real-time-notification-system-websocket)
11. [Error Handling & Response Standardization](#11-error-handling--response-standardization)
12. [Database Migration Strategy (Flyway)](#12-database-migration-strategy-flyway)
13. [Testing Strategy](#13-testing-strategy)
14. [Deployment & Infrastructure (Docker)](#14-deployment--infrastructure-docker)
15. [API Documentation (Swagger / OpenAPI 3.0)](#15-api-documentation-swagger--openapi-30)
16. [Performance Requirements & SLAs](#16-performance-requirements--slas)
17. [Milestones & Delivery Timeline](#17-milestones--delivery-timeline)

---

## 1. Executive Summary

The Movie Recommendation System addresses the "information overload" problem by providing personalized movie suggestions through a robust, scalable RESTful API backend. This proposal defines the complete backend architecture — from database schema to deployment — ensuring the system can be developed, tested, and validated independently of any frontend client.

**Key design goals:**

- **Independence** — The backend is a standalone, self-contained API server. Every feature is testable via Swagger UI or Postman without any frontend dependency.
- **Security-first** — Stateless JWT authentication with strict Role-Based Access Control (RBAC) protects every endpoint.
- **Performance** — Redis caching, async recommendation processing, and optimized queries guarantee sub-second response times on critical paths.
- **Maintainability** — Feature-based package structure, Flyway-managed migrations, and standardized response formats keep the codebase predictable as it scales.

---

## 2. System Architecture

### 2.1 High-Level Architecture Diagram

```
+-----------------------------------------------------------------------+
|                             CLIENT LAYER                              |
|              Next.js Frontend  |  Postman  |  Swagger UI              |
+----------------------------------+------------------------------------+
                                   | HTTPS (REST + WebSocket)
                                   v
+-----------------------------------------------------------------------+
|                  SPRING BOOT  --  Embedded Tomcat                     |
|                                                                       |
|              +----------------------------+                           |
|              | JWT Authentication Filter  |                           |
|              +-------------+--------------+                           |
|                            |                                          |
|    +------------+  +-------v--------+  +--------------------+         |
|    | WebSocket  |  |     REST       |  | Swagger / OpenAPI  |         |
|    | Handlers   |  | Controllers    |  | Documentation      |         |
|    +------+-----+  +-------+--------+  +--------------------+         |
|           |                |                                          |
|           |     +----------v-----------+                              |
|           |     |   Service Layer      |                              |
|           +---->|  (Business Logic)    |<---- @Async / @Scheduled     |
|                 +----------+-----------+                              |
|                            |                                          |
|                 +----------v-----------+    +------------------+      |
|                 | Repository Layer     |--->|   Redis Cache    |      |
|                 | (Spring Data JPA)    |    |   (TTL-managed)  |      |
|                 +----------+-----------+    +------------------+      |
|                            |                                          |
+----------------------------+------------------------------------------+
                             |
                  +----------v-----------+
                  |    PostgreSQL 16     |
                  |  (Primary Database)  |
                  |   Flyway-managed     |
                  +----------------------+
```

### 2.2 Layered Architecture

| Layer          | Responsibility                                                                   | Spring Annotations                     |
|----------------|----------------------------------------------------------------------------------|----------------------------------------|
| **Controller** | Accept HTTP requests, validate input DTOs, delegate to Service, return responses | `@RestController`, `@PreAuthorize`     |
| **Service**    | Business logic, transaction management, async processing, orchestration          | `@Service`, `@Transactional`, `@Async` |
| **Repository** | Data access abstraction, custom queries, cache integration                       | `@Repository`, `@Cacheable`            |
| **Entity**     | JPA-mapped domain objects, database table representation                         | `@Entity`, `@Table`                    |

**Strict rules:**
- Controllers never access Repositories directly.
- Entities never leak into API responses — all external communication uses DTOs.
- Services are the only layer that orchestrates cross-module calls.

---

## 3. Technology Stack & Justification

| Component            | Technology              | Version | Justification                                                          |
|----------------------|-------------------------|---------|------------------------------------------------------------------------|
| Language             | Java                    | 21 LTS  | Long-term support, virtual threads, pattern matching, Spring ecosystem |
| Framework            | Spring Boot             | 3.x     | Production-grade, massive ecosystem, auto-configuration                |
| Database             | PostgreSQL              | 16      | ACID compliance, advanced indexing, full-text search, JSON support     |
| Cache                | Redis                   | 7       | Sub-millisecond latency, TTL support, pub/sub for notifications        |
| Migration            | Flyway                  | 10.x    | Version-controlled schema, repeatable migrations, rollback support     |
| Security             | Spring Security + JWT   | —       | Stateless auth, fine-grained RBAC, battle-tested                       |
| Real-time            | Spring WebSocket (STOMP)| —       | Native Spring integration, topic-based pub/sub                         |
| Documentation        | SpringDoc OpenAPI       | 2.x     | Auto-generated Swagger UI from annotations, OpenAPI 3.0 spec           |
| Containerization     | Docker + Docker Compose | —       | Reproducible environments, single-command deployment                   |
| Testing              | JUnit 5 + Mockito       | —       | Industry standard, Spring Boot Test integration                        |

---

## 4. Project Structure

```
movie-recommendation-backend/
├── docker-compose.yml
├── Dockerfile
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew / gradlew.bat
│
├── src/
│   ├── main/
│   │   ├── java/com/movie/recommendation/
│   │   │   │
│   │   │   ├── RecommendationApplication.java          # @SpringBootApplication entry point
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java                 # Spring Security filter chain
│   │   │   │   ├── RedisConfig.java                    # Redis connection + serializer
│   │   │   │   ├── WebSocketConfig.java                # STOMP broker configuration
│   │   │   │   ├── SwaggerConfig.java                  # OpenAPI 3.0 metadata
│   │   │   │   ├── AsyncConfig.java                    # Thread pool for @Async tasks
│   │   │   │   └── CorsConfig.java                     # CORS policy for frontend origins
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java               # Token generation & validation
│   │   │   │   ├── JwtAuthenticationFilter.java        # OncePerRequestFilter for JWT
│   │   │   │   ├── JwtAuthEntryPoint.java              # 401 handler for unauthenticated
│   │   │   │   ├── CustomUserDetails.java              # UserDetails implementation
│   │   │   │   └── CustomUserDetailsService.java       # Loads user from DB for auth
│   │   │   │
│   │   │   ├── common/
│   │   │   │   ├── dto/
│   │   │   │   │   ├── ApiResponse.java                # Standardized { status, message, data }
│   │   │   │   │   └── PagedResponse.java              # Paginated response wrapper
│   │   │   │   ├── constants/
│   │   │   │   │   └── AppConstants.java               # Shared constants (page sizes, TTLs)
│   │   │   │   └── util/
│   │   │   │       └── SlugUtil.java                   # URL-safe slug generation
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java         # @ControllerAdvice
│   │   │   │   ├── ResourceNotFoundException.java      # → 404
│   │   │   │   ├── DuplicateResourceException.java     # → 409
│   │   │   │   ├── UnauthorizedException.java          # → 401
│   │   │   │   └── BadRequestException.java            # → 400
│   │   │   │
│   │   │   └── modules/
│   │   │       │
│   │   │       ├── auth/
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── AuthService.java
│   │   │       │   └── dto/
│   │   │       │       ├── LoginRequest.java
│   │   │       │       ├── RegisterRequest.java
│   │   │       │       ├── TokenRefreshRequest.java
│   │   │       │       └── AuthResponse.java
│   │   │       │
│   │   │       ├── user/
│   │   │       │   ├── UserController.java
│   │   │       │   ├── UserService.java
│   │   │       │   ├── UserRepository.java
│   │   │       │   ├── entity/
│   │   │       │   │   ├── User.java
│   │   │       │   │   └── Role.java                   # Enum: ADMIN, USER, GUEST
│   │   │       │   └── dto/
│   │   │       │       ├── UserProfileResponse.java
│   │   │       │       └── UpdateProfileRequest.java
│   │   │       │
│   │   │       ├── movie/
│   │   │       │   ├── MovieController.java
│   │   │       │   ├── MovieService.java
│   │   │       │   ├── MovieRepository.java
│   │   │       │   ├── entity/
│   │   │       │   │   ├── Movie.java
│   │   │       │   │   └── Genre.java
│   │   │       │   └── dto/
│   │   │       │       ├── MovieResponse.java
│   │   │       │       ├── MovieDetailResponse.java
│   │   │       │       ├── CreateMovieRequest.java
│   │   │       │       └── MovieSearchCriteria.java
│   │   │       │
│   │   │       ├── review/
│   │   │       │   ├── ReviewController.java
│   │   │       │   ├── ReviewService.java
│   │   │       │   ├── ReviewRepository.java
│   │   │       │   ├── entity/
│   │   │       │   │   ├── Review.java
│   │   │       │   │   ├── ReviewLike.java
│   │   │       │   │   └── ReviewReply.java
│   │   │       │   └── dto/
│   │   │       │       ├── CreateReviewRequest.java
│   │   │       │       ├── ReviewResponse.java
│   │   │       │       └── ReplyResponse.java
│   │   │       │
│   │   │       ├── watchlist/
│   │   │       │   ├── WatchlistController.java
│   │   │       │   ├── WatchlistService.java
│   │   │       │   ├── WatchlistRepository.java
│   │   │       │   ├── entity/
│   │   │       │   │   └── WatchlistItem.java
│   │   │       │   └── dto/
│   │   │       │       └── WatchlistResponse.java
│   │   │       │
│   │   │       ├── recommendation/
│   │   │       │   ├── RecommendationScheduler.java    # @Scheduled cronjob (every 6h)
│   │   │       │   ├── RecommendationService.java
│   │   │       │   ├── RecommendationRepository.java
│   │   │       │   ├── strategy/
│   │   │       │   │   ├── RecommendationStrategy.java # Strategy interface
│   │   │       │   │   ├── CollaborativeFilteringStrategy.java
│   │   │       │   │   └── ContentBasedStrategy.java
│   │   │       │   ├── entity/
│   │   │       │   │   └── Recommendation.java
│   │   │       │   └── dto/
│   │   │       │       └── RecommendationResponse.java
│   │   │       │
│   │   │       └── notification/
│   │   │           ├── NotificationController.java
│   │   │           ├── NotificationService.java
│   │   │           ├── NotificationRepository.java
│   │   │           ├── WebSocketEventHandler.java      # Observer pattern impl
│   │   │           ├── entity/
│   │   │           │   └── Notification.java
│   │   │           └── dto/
│   │   │               └── NotificationResponse.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml                         # Main config
│   │       ├── application-dev.yml                     # Dev profile overrides
│   │       ├── application-prod.yml                    # Prod profile overrides
│   │       └── db/migration/
│   │           ├── V1__create_users_and_roles.sql
│   │           ├── V2__create_movies_and_genres.sql
│   │           ├── V3__create_reviews.sql
│   │           ├── V4__create_watchlist.sql
│   │           ├── V5__create_recommendations.sql
│   │           └── V6__create_notifications.sql
│   │
│   └── test/
│       └── java/com/movie/recommendation/
│           ├── modules/
│           │   ├── auth/AuthServiceTest.java
│           │   ├── movie/MovieServiceTest.java
│           │   ├── review/ReviewServiceTest.java
│           │   └── recommendation/RecommendationServiceTest.java
│           └── integration/
│               ├── AuthIntegrationTest.java
│               └── MovieIntegrationTest.java
│
├── postman/
│   └── Movie_Recommendation_API.postman_collection.json
│
└── docs/
    └── proposal.md                                     # This document
```

---

## 5. Database Schema Design (PostgreSQL)

### 5.1 Entity-Relationship Diagram

```
+----------------+         +------------------+       +----------------+
|     users      |         |  movie_genres    |       |     genres     |
+----------------+         |  (join table)    |       +----------------+
| id (PK)        |         +------------------+       | id (PK)        |
| email (UQ)     |         | movie_id (FK) ---+--+    | name (UQ)      |
| username (UQ)  |         | genre_id (FK) ---+--+--->| slug (UQ)      |
| password_hash  |         +------------------+  |    | created_at     |
| role           |                               |    +----------------+
| avatar_url     |         +------------------+  |
| preferences    |         |     movies       |<-+
| is_active      |         +------------------+
| created_at     |         | id (PK)          |
| updated_at     |         | title            |
+-------+--------+         | slug (UQ)        |
        |                  | overview         |
        |                  | poster_url       |
        |                  | backdrop_url     |
        |                  | release_date     |
        |                  | runtime_minutes  |
        |                  | avg_rating       |
        |                  | vote_count       |
        |                  | is_active        |
        |                  | created_at       |
        |                  | updated_at       |
        |                  +-------+----------+
        |                          |
        |     +--------------------+---------------------+
        |     |                    |                     |
        v     v                    v                     v
+------------------+  +--------------+  +---------------------+
|    reviews       |  |  watchlist   |  |  recommendations    |
+------------------+  +--------------+  +---------------------+
| id (PK)          |  | id (PK)      |  | id (PK)             |
| user_id (FK)     |  | user_id (FK) |  | user_id (FK)        |
| movie_id (FK)    |  | movie_id(FK) |  | movie_id (FK)       |
| rating (1-5)     |  | added_at     |  | score               |
| content          |  +--------------+  | strategy_type       |
| is_spoiler       |  UQ(user,movie)    | generated_at        |
| created_at       |                    +---------------------+
| updated_at       |                    UQ(user,movie,strategy)
+-------+----------+
        | UQ(user_id, movie_id)
        |
        +---------------------+
        |                     |
        v                     v
+------------------+  +------------------+
|  review_likes    |  |  review_replies  |
+------------------+  +------------------+
| id (PK)          |  | id (PK)          |
| review_id (FK)   |  | review_id (FK)   |
| user_id (FK)     |  | user_id (FK)     |
| created_at       |  | content          |
+------------------+  | created_at       |
UQ(review,user)       | updated_at       |
                      +------------------+

+------------------+
|  notifications   |
+------------------+
| id (PK)          |
| recipient_id(FK) |
| actor_id (FK)    |
| type (ENUM)      |
| reference_id     |
| message          |
| is_read          |
| created_at       |
+------------------+
```

### 5.2 Table Definitions (DDL)

#### V1 — Users & Roles

```sql
CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL DEFAULT 'USER'
                                    CHECK (role IN ('ADMIN', 'USER', 'GUEST')),
    avatar_url      VARCHAR(500),
    preferences     JSONB           DEFAULT '[]'::jsonb,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email    ON users (email);
CREATE INDEX idx_users_role     ON users (role);
CREATE INDEX idx_users_active   ON users (is_active) WHERE is_active = TRUE;
```

> **`preferences`** stores the user's initial genre/topic preferences as a JSONB array (e.g., `["action", "sci-fi"]`). This is used by the recommendation engine to solve the **Cold Start** problem for new users who have no rating history.

#### V2 — Movies & Genres

```sql
CREATE TABLE genres (
    id          SERIAL          PRIMARY KEY,
    name        VARCHAR(50)     NOT NULL UNIQUE,
    slug        VARCHAR(50)     NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE movies (
    id              BIGSERIAL       PRIMARY KEY,
    title           VARCHAR(500)    NOT NULL,
    slug            VARCHAR(500)    NOT NULL UNIQUE,
    overview        TEXT,
    poster_url      VARCHAR(500),
    backdrop_url    VARCHAR(500),
    release_date    DATE,
    runtime_minutes INTEGER,
    avg_rating      NUMERIC(3,2)   NOT NULL DEFAULT 0.00,
    vote_count      INTEGER        NOT NULL DEFAULT 0,
    is_active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE movie_genres (
    movie_id    BIGINT  NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    genre_id    INTEGER NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, genre_id)
);

CREATE INDEX idx_movies_active      ON movies (is_active) WHERE is_active = TRUE;
CREATE INDEX idx_movies_release     ON movies (release_date DESC);
CREATE INDEX idx_movies_rating      ON movies (avg_rating DESC);
CREATE INDEX idx_movies_title_trgm  ON movies USING gin (title gin_trgm_ops);
```

> **`idx_movies_title_trgm`** uses the `pg_trgm` extension for fuzzy/partial-match search on movie titles. Requires `CREATE EXTENSION IF NOT EXISTS pg_trgm;` in V0.

#### V3 — Reviews, Likes & Replies

```sql
CREATE TABLE reviews (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    movie_id    BIGINT          NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    rating      SMALLINT        NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content     TEXT,
    is_spoiler  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, movie_id)
);

CREATE TABLE review_likes (
    id          BIGSERIAL   PRIMARY KEY,
    review_id   BIGINT      NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (review_id, user_id)
);

CREATE TABLE review_replies (
    id          BIGSERIAL   PRIMARY KEY,
    review_id   BIGINT      NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content     TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reviews_movie  ON reviews (movie_id);
CREATE INDEX idx_reviews_user   ON reviews (user_id);
CREATE INDEX idx_replies_review ON review_replies (review_id);
```

> **`UNIQUE (user_id, movie_id)`** on `reviews` enforces the business rule: one review per user per movie.

#### V4 — Watchlist

```sql
CREATE TABLE watchlist (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    movie_id    BIGINT      NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    added_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, movie_id)
);

CREATE INDEX idx_watchlist_user ON watchlist (user_id);
```

#### V5 — Recommendations

```sql
CREATE TABLE recommendations (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    movie_id        BIGINT          NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    score           NUMERIC(5,4)    NOT NULL,
    strategy_type   VARCHAR(30)     NOT NULL
                                    CHECK (strategy_type IN ('COLLABORATIVE', 'CONTENT_BASED')),
    generated_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, movie_id, strategy_type)
);

CREATE INDEX idx_recommendations_user   ON recommendations (user_id, score DESC);
```

> This table is **write-heavy** (bulk-refreshed every 6 hours by the cron job) and **read-heavy** (queried on every homepage load). The cron job truncates stale rows and inserts fresh ones within a single transaction.

#### V6 — Notifications

```sql
CREATE TABLE notifications (
    id              BIGSERIAL       PRIMARY KEY,
    recipient_id    BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id        BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type            VARCHAR(30)     NOT NULL
                                    CHECK (type IN ('REVIEW_LIKE', 'REVIEW_REPLY', 'NEW_RECOMMENDATION')),
    reference_id    BIGINT,
    message         TEXT            NOT NULL,
    is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_recipient ON notifications (recipient_id, is_read, created_at DESC);
```

### 5.3 Soft Delete Strategy

Movies are **never physically deleted**. The `is_active` column controls visibility:

| Operation           | SQL Effect                            | API Behavior                                       |
|---------------------|---------------------------------------|----------------------------------------------------|
| Admin "deletes"     | `UPDATE movies SET is_active = false` | Movie hidden from search/browse                    |
| Admin restores      | `UPDATE movies SET is_active = true`  | Movie reappears in catalog                         |
| User queries        | `WHERE is_active = TRUE`              | Only active movies returned                        |
| Historical reviews  | Remain intact                         | Reviews on deactivated movies still count for user |

---

## 6. API Design Standards & Conventions

### 6.1 URL Structure

All endpoints follow RESTful conventions under a versioned base path:

```
Base URL: /api/v1
```

| Convention                   | Example                                      |
|------------------------------|----------------------------------------------|
| Plural nouns for resources   | `/api/v1/movies`, `/api/v1/users`            |
| Nested for relationships     | `/api/v1/movies/{id}/reviews`                |
| Verbs only for actions       | `/api/v1/auth/login`, `/api/v1/auth/refresh` |
| Pagination via query params  | `?page=0&size=20&sort=createdAt,desc`        |
| Filtering via query params   | `?genre=action&year=2024&minRating=4`        |

### 6.2 HTTP Methods & Status Codes

| Method   | Purpose           | Success Code | Notes                                      |
|----------|-------------------|--------------|--------------------------------------------|
| `GET`    | Retrieve resource | `200`        | Never modifies state                       |
| `POST`   | Create resource   | `201`        | Returns created resource + Location header |
| `PUT`    | Full update       | `200`        | Replaces the entire resource               |
| `PATCH`  | Partial update    | `200`        | Updates only specified fields              |
| `DELETE` | Remove resource   | `204`        | No body returned                           |

**Error codes used consistently:**

| Code  | Meaning                  | When Used                                     |
|-------|--------------------------|-----------------------------------------------|
| `400` | Bad Request              | Validation failure, malformed input           |
| `401` | Unauthorized             | Missing or expired JWT token                  |
| `403` | Forbidden                | Valid token but insufficient role/permissions |
| `404` | Not Found                | Resource doesn't exist or is soft-deleted     |
| `409` | Conflict                 | Duplicate (e.g., user already reviewed movie) |
| `429` | Too Many Requests        | Rate limit exceeded                           |
| `500` | Internal Server Error    | Unhandled exception (logged, not leaked)      |

### 6.3 Complete API Endpoint Reference

#### Auth Module

| Method | Endpoint                       | Access  | Description                      | Request Body             | Response              |
|--------|--------------------------------|---------|----------------------------------|--------------------------|-----------------------|
| POST   | `/api/v1/auth/register`        | Public  | Register a new user              | `RegisterRequest`        | `AuthResponse` (201)  |
| POST   | `/api/v1/auth/login`           | Public  | Authenticate and receive tokens  | `LoginRequest`           | `AuthResponse` (200)  |
| POST   | `/api/v1/auth/refresh`         | Public  | Refresh access token via cookie  | —                        | `AuthResponse` (200)  |
| POST   | `/api/v1/auth/logout`          | USER+   | Invalidate refresh token         | —                        | 204                   |
| POST   | `/api/v1/auth/forgot-password` | Public  | Send password reset email        | `{ email }`              | 200                   |
| POST   | `/api/v1/auth/reset-password`  | Public  | Reset password with token        | `{ token, newPassword }` | 200                   |

#### User Module

| Method | Endpoint                       | Access  | Description                         |
|--------|--------------------------------|---------|-------------------------------------|
| GET    | `/api/v1/users/me`             | USER+   | Get current user's profile          |
| PUT    | `/api/v1/users/me`             | USER+   | Update current user's profile       |
| PUT    | `/api/v1/users/me/preferences` | USER+   | Update genre preferences            |
| GET    | `/api/v1/users/{id}`           | ADMIN   | Get any user's profile              |
| GET    | `/api/v1/users`                | ADMIN   | List all users (paginated)          |
| PATCH  | `/api/v1/users/{id}/role`      | ADMIN   | Change a user's role                |
| DELETE | `/api/v1/users/{id}`           | ADMIN   | Deactivate a user account           |

#### Movie Module

| Method | Endpoint                      | Access  | Description                         |
|--------|-------------------------------|---------|-------------------------------------|
| GET    | `/api/v1/movies`              | Public  | List movies (paginated, filterable) |
| GET    | `/api/v1/movies/{id}`         | Public  | Get movie details                   |
| GET    | `/api/v1/movies/search`       | Public  | Full-text search by title           |
| GET    | `/api/v1/movies/trending`     | Public  | Get trending movies (cached)        |
| POST   | `/api/v1/movies`              | ADMIN   | Create a new movie                  |
| PUT    | `/api/v1/movies/{id}`         | ADMIN   | Update movie details                |
| DELETE | `/api/v1/movies/{id}`         | ADMIN   | Soft-delete a movie                 |
| PATCH  | `/api/v1/movies/{id}/restore` | ADMIN   | Restore a soft-deleted movie        |

#### Genre Module

| Method | Endpoint                    | Access  | Description                         |
|--------|-----------------------------|---------|-------------------------------------|
| GET    | `/api/v1/genres`            | Public  | List all genres                     |
| POST   | `/api/v1/genres`            | ADMIN   | Create a genre                      |
| PUT    | `/api/v1/genres/{id}`       | ADMIN   | Update a genre                      |
| DELETE | `/api/v1/genres/{id}`       | ADMIN   | Delete a genre                      |

#### Review Module

| Method | Endpoint                              | Access  | Description                          |
|--------|---------------------------------------|---------|--------------------------------------|
| GET    | `/api/v1/movies/{movieId}/reviews`    | Public  | List reviews for a movie (paginated) |
| POST   | `/api/v1/movies/{movieId}/reviews`    | USER+   | Submit a review (1 per user/movie)   |
| PUT    | `/api/v1/reviews/{id}`                | Owner   | Edit own review                      |
| DELETE | `/api/v1/reviews/{id}`                | Owner/ADMIN | Delete a review                  |
| POST   | `/api/v1/reviews/{id}/like`           | USER+   | Like a review (toggle)               |
| POST   | `/api/v1/reviews/{id}/replies`        | USER+   | Reply to a review                    |
| GET    | `/api/v1/reviews/{id}/replies`        | Public  | List replies for a review            |

#### Watchlist Module

| Method | Endpoint                              | Access  | Description                          |
|--------|---------------------------------------|---------|--------------------------------------|
| GET    | `/api/v1/watchlist`                   | USER+   | Get user's watchlist (paginated)     |
| POST   | `/api/v1/watchlist/{movieId}`         | USER+   | Add movie to watchlist               |
| DELETE | `/api/v1/watchlist/{movieId}`         | USER+   | Remove movie from watchlist          |

#### Recommendation Module

| Method | Endpoint                              | Access  | Description                          |
|--------|---------------------------------------|---------|--------------------------------------|
| GET    | `/api/v1/recommendations`             | USER+   | Get personalized recommendations     |
| POST   | `/api/v1/recommendations/refresh`     | ADMIN   | Manually trigger recommendation job  |

#### Notification Module

| Method | Endpoint                              | Access  | Description                          |
|--------|---------------------------------------|---------|--------------------------------------|
| GET    | `/api/v1/notifications`               | USER+   | List user's notifications (paginated)|
| PATCH  | `/api/v1/notifications/{id}/read`     | USER+   | Mark notification as read            |
| PATCH  | `/api/v1/notifications/read-all`      | USER+   | Mark all notifications as read       |
| GET    | `/api/v1/notifications/unread-count`  | USER+   | Get count of unread notifications    |

### 6.4 Request / Response DTO Examples

#### Standardized API Response Wrapper

Every endpoint returns responses in this format:

```json
// Success (single resource)
{
  "status": 200,
  "message": "Movie retrieved successfully",
  "data": {
    "id": 1,
    "title": "Inception",
    "slug": "inception",
    "overview": "A thief who steals corporate secrets...",
    "genres": ["Action", "Sci-Fi", "Thriller"],
    "avgRating": 4.72,
    "voteCount": 1843,
    "releaseDate": "2010-07-16",
    "runtimeMinutes": 148,
    "posterUrl": "https://image.tmdb.org/t/p/w500/..."
  }
}

// Success (paginated list)
{
  "status": 200,
  "message": "Movies retrieved successfully",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 342,
    "totalPages": 18,
    "last": false
  }
}

// Error
{
  "status": 409,
  "message": "You have already reviewed this movie",
  "data": null
}

// Validation Error
{
  "status": 400,
  "message": "Validation failed",
  "data": {
    "email": "must be a valid email address",
    "password": "must be at least 8 characters"
  }
}
```

#### Register Request

```json
POST /api/v1/auth/register
{
  "email": "john@example.com",
  "username": "johndoe",
  "password": "SecureP@ss123",
  "preferences": ["action", "sci-fi"]
}
```

#### Auth Response

```json
{
  "status": 201,
  "message": "Registration successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": 42,
      "email": "john@example.com",
      "username": "johndoe",
      "role": "USER"
    }
  }
}
```

> **Note:** The Refresh Token is set as an `HttpOnly`, `Secure`, `SameSite=Strict` cookie — it never appears in the response body.

---

## 7. Authentication & Authorization (JWT + RBAC)

### 7.1 Authentication Flow

```
  Client                       Spring Boot                    PostgreSQL
    |                               |                               |
    |  POST /api/v1/auth/login      |                               |
    |  { email, password }          |                               |
    |------------------------------>|                               |
    |                               |  SELECT * FROM users          |
    |                               |  WHERE email = ?              |
    |                               |------------------------------>|
    |                               |<------------------------------|
    |                               |                               |
    |                               |  BCrypt.verify(password, hash)|
    |                               |  Generate Access Token (15min)|
    |                               |  Generate Refresh Token (7d)  |
    |                               |                               |
    |  200 OK                       |                               |
    |  Body: { accessToken }        |                               |
    |  Cookie: refreshToken(HttpOnly|                               |
    |<------------------------------|                               |
    |                               |                               |
    |  GET /api/v1/movies           |                               |
    |  Authorization: Bearer <token>|                               |
    |------------------------------>|                               |
    |                               |  JwtAuthFilter:               |
    |                               |  1. Extract token from header |
    |                               |  2. Validate signature + exp  |
    |                               |  3. Load UserDetails          |
    |                               |  4. Set SecurityContext       |
    |                               |                               |
    |  200 OK { movies }            |                               |
    |<------------------------------|                               |
    |                               |                               |
    |  POST /api/v1/auth/refresh    |                               |
    |  Cookie: refreshToken         |                               |
    |------------------------------>|                               |
    |                               |  Validate refresh token       |
    |                               |  Issue new access token       |
    |  200 OK { newAccessToken }    |                               |
    |<------------------------------|                               |
```

### 7.2 Token Specifications

| Property       | Access Token                           | Refresh Token                                    |
|----------------|----------------------------------------|--------------------------------------------------|
| Lifetime       | 15 minutes                             | 7 days                                           |
| Algorithm      | HS512                                  | HS512                                            |
| Storage        | Client memory / `Authorization` header | `HttpOnly` + `Secure` + `SameSite=Strict` cookie |
| Payload claims | `sub` (userId), `role`, `iat`, `exp`   | `sub` (userId), `iat`, `exp`, `jti` (unique ID)  |
| Revocation     | Short TTL (self-expires)               | Stored in DB; revoked on logout                  |

### 7.3 JWT Filter Chain

```java
// SecurityConfig.java — Conceptual configuration
http
    .csrf(csrf -> csrf.disable())
    .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
    .exceptionHandling(eh -> eh.authenticationEntryPoint(jwtAuthEntryPoint))
    .authorizeHttpRequests(auth -> auth
        // Public endpoints
        .requestMatchers("/api/v1/auth/**").permitAll()
        .requestMatchers("/api/v1/movies/search").permitAll()
        .requestMatchers("/api/v1/movies/trending").permitAll()
        .requestMatchers(GET, "/api/v1/movies/**").permitAll()
        .requestMatchers(GET, "/api/v1/genres/**").permitAll()
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
        // Admin-only endpoints
        .requestMatchers(POST, "/api/v1/movies").hasRole("ADMIN")
        .requestMatchers(PUT, "/api/v1/movies/**").hasRole("ADMIN")
        .requestMatchers(DELETE, "/api/v1/movies/**").hasRole("ADMIN")
        .requestMatchers("/api/v1/users").hasRole("ADMIN")
        // All other endpoints require authentication
        .anyRequest().authenticated()
    )
    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```

### 7.4 RBAC Permission Matrix

| Endpoint Category            | GUEST | USER  | ADMIN  |
|------------------------------|-------|-------|--------|
| Browse/search movies         |  ✅   |  ✅  |  ✅   |
| View movie details & reviews |  ✅   |  ✅  |  ✅   |
| Register / Login             |  ✅   |  ✅  |  ✅   |
| Submit / edit reviews        |  ❌   |  ✅  |  ✅   |
| Like / reply to reviews      |  ❌   |  ✅  |  ✅   |
| Manage watchlist             |  ❌   |  ✅  |  ✅   |
| View recommendations         |  ❌   |  ✅  |  ✅   |
| View notifications           |  ❌   |  ✅  |  ✅   |
| Create / update movies       |  ❌   |  ❌  |  ✅   |
| Soft-delete / restore movies |  ❌   |  ❌  |  ✅   |
| Manage genres                |  ❌   |  ❌  |  ✅   |
| Manage users / change roles  |  ❌   |  ❌  |  ✅   |
| Trigger recommendation job   |  ❌   |  ❌  |  ✅   |

### 7.5 Password Policy

| Rule                  | Requirement                                                     |
|-----------------------|-----------------------------------------------------------------|
| Minimum length        | 8 characters                                                    |
| Complexity            | At least 1 uppercase, 1 lowercase, 1 digit, 1 special character |
| Hashing               | BCrypt with strength 12                                         |
| Comparison            | Constant-time comparison (Spring Security default)              |

---

## 8. Caching Strategy (Redis)

### 8.1 Cache Architecture

```
+------------------------------------------------------+
|                   Service Layer                      |
|                                                      |
|   @Cacheable("trending")        Cache HIT --> Return |
|   getTrendingMovies() ----------------------> Redis  |
|                                 Cache MISS --> DB    |
|                                       |              |
|   @CacheEvict("trending")            v               |
|   createMovie() ----------> Invalidate Cache         |
|                                                      |
|   @CachePut("movie::{id}")                           |
|   updateMovie() ----------> Update Cache Entry       |
+------------------------------------------------------+
```

### 8.2 Cache Key Design

| Cache Name             | Key Pattern                           | TTL       | Eviction Trigger                    |
|------------------------|---------------------------------------|-----------|-------------------------------------|
| `trending_movies`      | `trending::page:{page}:size:{size}`   | 1 hour    | New movie created, rating updated   |
| `movie_detail`         | `movie::{movieId}`                    | 1 hour    | Movie updated or deleted            |
| `movie_search`         | `search::{query}:page:{p}:size:{s}`   | 30 min    | New movie created                   |
| `genres_all`           | `genres::all`                         | 24 hours  | Genre created / updated / deleted   |
| `user_recommendations` | `recommendations::user:{userId}`      | 6 hours   | Recommendation cron job completes   |
| `user_notifications`   | `notifications::user:{userId}:unread` | 5 min     | New notification or mark-as-read    |

### 8.3 Cache Configuration

```yaml
# application.yml — Redis configuration
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # Default 1 hour in ms
      cache-null-values: false
      key-prefix: "movie-rec::"
```

### 8.4 Cache Invalidation Rules

1. **Write-through for single entities** — When a movie is updated, `@CachePut` updates the cached entry immediately.
2. **Evict-on-write for collections** — When a movie is created or deleted, `@CacheEvict` removes the `trending` and `search` caches to force a fresh query.
3. **Scheduled expiry for recommendations** — The 6-hour cron job writes new recommendations and evicts the `user_recommendations` cache for all affected users.
4. **Short TTL for real-time data** — Notification counts use a 5-minute TTL since WebSocket handles real-time delivery; the cache is only a fallback for page refreshes.

---

## 9. Recommendation Engine

### 9.1 Strategy Pattern Implementation

```
          +-----------------------------------+
          |     RecommendationStrategy        |  <<interface>>
          |  + recommend(userId): List<Rec>   |
          +----------------+------------------+
                           |
             +-------------+---------------+
             |                             |
             v                             v
+----------------------------+  +--------------------------------+
| CollaborativeFiltering     |  | ContentBasedStrategy           |
| Strategy                   |  |                                |
|                            |  |                                |
| "Users who liked X also    |  | "Movies similar to what you    |
|  liked Y"                  |  |  already enjoyed"              |
|                            |  |                                |
| Uses: user-item rating     |  | Uses: genre overlap, keyword   |
| matrix, cosine similarity  |  | matching, release period       |
+----------------------------+  +--------------------------------+
```

### 9.2 Recommendation Workflow

```
+--------------------------------------------------------------------+
|                    @Scheduled (every 6 hours)                      |
|                    RecommendationScheduler                         |
+--------------------------------------------------------------------+
|                                                                    |
|  1. Fetch all active users with >= 5 reviews                       |
|     +---> Apply CollaborativeFilteringStrategy                     |
|                                                                    |
|  2. Fetch all active users with < 5 reviews (Cold Start)           |
|     +---> Apply ContentBasedStrategy using user.preferences        |
|                                                                    |
|  3. For each user:                                                 |
|     a. Calculate scores for candidate movies                       |
|     b. Rank top-N (default: 20) movies                             |
|     c. DELETE FROM recommendations WHERE user_id = ?               |
|     d. INSERT new ranked recommendations                           |
|                                                                    |
|  4. Evict Redis cache: recommendations::user:*                     |
|                                                                    |
|  5. Log: "Recommendation job completed in {duration}ms             |
|           for {userCount} users"                                   |
+--------------------------------------------------------------------+
```

### 9.3 Cold Start Handling

| User State              | Strategy Applied      | Data Source                          |
|-------------------------|-----------------------|--------------------------------------|
| New user (0 reviews)    | Content-Based         | `users.preferences` (JSONB)          |
| Light user (1-4 reviews)| Content-Based         | `users.preferences` + review history |
| Active user (5+ reviews)| Collaborative Filtering | Full review/rating history         |

---

## 10. Real-Time Notification System (WebSocket)

### 10.1 Architecture

```
+-----------+    STOMP/WS     +--------------------+
|  Client   |<--------------->|  Spring WebSocket  |
|  (SockJS) |  /ws endpoint   |  STOMP Broker      |
+-----------+                 +---------+----------+
                                        |
                              +---------v-----------+
                              | NotificationService |
                              | (Observer Pattern)  |
                              +---------+-----------+
                                        |
                              Triggered by:
                              - ReviewService.likeReview()
                              - ReviewService.replyToReview()
                              - RecommendationScheduler
```

### 10.2 WebSocket Endpoints

| STOMP Destination                     | Direction | Description                          |
|---------------------------------------|-----------|--------------------------------------|
| `/ws`                                 | Connect   | WebSocket handshake endpoint (SockJS)|
| `/user/queue/notifications`           | Server→Client | Private notification channel     |
| `/topic/trending`                     | Server→Client | Broadcast trending updates       |

### 10.3 Notification Event Types

| Event               | Trigger                      | Payload                                                   |
|---------------------|------------------------------|-----------------------------------------------------------|
| `REVIEW_LIKE`       | User likes a review          | `{ type, actorName, movieTitle, reviewId }`               |
| `REVIEW_REPLY`      | User replies to a review     | `{ type, actorName, movieTitle, reviewId, replyPreview }` |

---

## 11. Error Handling & Response Standardization

### 11.1 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 — Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) { ... }

    // 409 — Duplicate / Conflict
    @ExceptionHandler(DuplicateResourceException.class)
    ResponseEntity<ApiResponse<Void>> handleConflict(DuplicateResourceException ex) { ... }

    // 400 — Validation Errors (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(...) { ... }

    // 401 — Authentication Failure
    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) { ... }

    // 403 — Access Denied
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handleForbidden(AccessDeniedException ex) { ... }

    // 500 — Catch-All (log full stack, return safe message)
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) { ... }
}
```

### 11.2 Standardized Error Format

Every error response follows the same `ApiResponse` structure:

```json
{
  "status": 404,
  "message": "Movie with id 999 not found",
  "data": null
}
```

For validation errors, `data` contains a field-to-message map:

```json
{
  "status": 400,
  "message": "Validation failed",
  "data": {
    "email": "must be a valid email address",
    "password": "size must be between 8 and 100",
    "rating": "must be between 1 and 5"
  }
}
```

---

## 12. Database Migration Strategy (Flyway)

### 12.1 Migration Naming Convention

```
V{version}__{description}.sql    — Versioned (runs once, in order)
R__{description}.sql             — Repeatable (re-run when checksum changes)
```

### 12.2 Migration Sequence

| File                              | Purpose                                           |
|-----------------------------------|---------------------------------------------------|
| `V0__enable_extensions.sql`       | `CREATE EXTENSION IF NOT EXISTS pg_trgm`          |
| `V1__create_users_and_roles.sql`  | Users table with RBAC role column                 |
| `V2__create_movies_and_genres.sql`| Movies, genres, junction table, trigram index     |
| `V3__create_reviews.sql`          | Reviews, likes, replies with unique constraints   |
| `V4__create_watchlist.sql`        | Watchlist with user-movie uniqueness              |
| `V5__create_recommendations.sql`  | Recommendation results table                      |
| `V6__create_notifications.sql`    | Notification events table                         |
| `V7__seed_genres.sql`             | Insert default genres (Action, Comedy, Drama, …)  |
| `V8__create_admin_user.sql`       | Insert default admin account (password from env)  |

### 12.3 Migration Rules

1. **Never edit a migration that has been applied** — create a new migration instead.
2. **Each migration is a single transaction** — either fully applied or fully rolled back.
3. **Schema-only in versioned migrations** — seed data goes in separate, clearly labeled migrations.
4. **Flyway runs automatically on application startup** via Spring Boot auto-configuration.

---

## 13. Testing Strategy

### 13.1 Testing Pyramid

```
              +----------------+
             /     E2E / API    \       Postman collections + Swagger UI
            /       Testing      \      Full request-response validation
           +----------------------+
          /       Integration      \     @SpringBootTest + Testcontainers
         /          Testing         \    Real DB + Redis, HTTP layer
        +----------------------------+
       /          Unit Testing        \  JUnit 5 + Mockito
      /                                \ Service layer logic in isolation
     +----------------------------------+
```

### 13.2 Unit Tests (Service Layer)

**Scope:** Test business logic in isolation with mocked dependencies.

| Module                | Test Cases                                                                                           |
|-----------------------|------------------------------------------------------------------------------------------------------|
| AuthService           | Register success, duplicate email rejection, login with wrong password, token generation correctness |
| MovieService          | CRUD operations, soft-delete behavior, search with various filters                                   |
| ReviewService         | Create review, enforce one-per-user-per-movie, rating avg recalculation                              |
| RecommendationService | Strategy selection based on review count, score ranking, cold start fallback                         |

**Example structure:**

```java
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock ReviewRepository reviewRepository;
    @Mock MovieRepository movieRepository;
    @InjectMocks ReviewService reviewService;

    @Test
    void createReview_duplicateReview_throwsConflict() {
        when(reviewRepository.existsByUserIdAndMovieId(1L, 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
            () -> reviewService.createReview(1L, 1L, request));
    }

    @Test
    void createReview_success_updatesMovieAvgRating() { ... }
}
```

### 13.3 Integration Tests (Full Stack)

**Scope:** Verify end-to-end behavior with real PostgreSQL and Redis using Testcontainers.

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class MovieIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

    @Autowired TestRestTemplate restTemplate;

    @Test
    void getMovies_returnsPagedResults() {
        ResponseEntity<ApiResponse> response =
            restTemplate.getForEntity("/api/v1/movies?page=0&size=10", ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
    }
}
```

### 13.4 API Testing (Postman + Swagger)

A Postman collection covering every endpoint is maintained alongside the codebase:

| Collection Folder  | Tests Verified                                                    |
|--------------------|-------------------------------------------------------------------|
| Auth               | Register → Login → Refresh → Logout lifecycle                     |
| Movies (Public)    | Browse, search, trending, detail                                  |
| Movies (Admin)     | Create, update, soft-delete, restore (with admin JWT)             |
| Reviews            | Create, duplicate rejection, like toggle, reply chain             |
| Watchlist          | Add, list, remove                                                 |
| Recommendations    | View personalized results, admin manual trigger                   |
| Notifications      | List, mark read, unread count                                     |
| Error Scenarios    | 400, 401, 403, 404, 409 responses match contract                  |

### 13.5 Test Coverage Targets

| Layer        | Coverage Target | Tool                    |
|--------------|-----------------|-------------------------|
| Service      | ≥ 80%           | JaCoCo                  |
| Repository   | ≥ 70%           | JaCoCo + Testcontainers |
| Controller   | ≥ 70%           | MockMvc                 |
| **Overall**  | **≥ 75%**       | JaCoCo aggregate report |

---

## 14. Deployment & Infrastructure (Docker)

> **For developers:** Use the Makefile commands to run the application. See the [README](../README.md) for quick start instructions. The configurations below are reference documentation for the Docker setup.

### 14.1 Docker Compose Architecture

```yaml
# docker-compose.yml
services:
  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: movie_rec
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy

  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: movie_rec
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER}"]
      interval: 5s
      timeout: 3s
      retries: 5

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 5

volumes:
  pgdata:
```

### 14.2 Dockerfile (Multi-Stage Build)

```dockerfile
# Stage 1: Build
FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon
COPY src ./src
RUN gradle bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 14.3 Environment Configuration

| Variable                 | Description                  | Default (Dev)     | Required (Prod)  |
|--------------------------|------------------------------|-------------------|:----------------:|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile        | `dev`             | Yes              |
| `DB_HOST`                | PostgreSQL hostname          | `localhost`       | Yes              |
| `DB_PORT`                | PostgreSQL port              | `5432`            | Yes              |
| `DB_NAME`                | Database name                | `movie_rec`       | Yes              |
| `DB_USER`                | Database user                | `postgres`        | Yes              |
| `DB_PASSWORD`            | Database password            | `postgres`        | Yes              |
| `REDIS_HOST`             | Redis hostname               | `localhost`       | Yes              |
| `REDIS_PORT`             | Redis port                   | `6379`            | Yes              |
| `JWT_SECRET`             | JWT signing key (≥ 64 chars) | —                 | Yes              |
| `JWT_ACCESS_EXPIRY`      | Access token lifetime (ms)   | `900000` (15min)  | No               |
| `JWT_REFRESH_EXPIRY`     | Refresh token lifetime (ms)  | `604800000` (7d)  | No               |

---

## 15. API Documentation (Swagger / OpenAPI 3.0)

### 15.1 Access Points

| URL                                      | Description                                 |
|------------------------------------------|---------------------------------------------|
| `http://localhost:8080/swagger-ui.html`  | Interactive Swagger UI                      |
| `http://localhost:8080/v3/api-docs`      | Raw OpenAPI 3.0 JSON spec                   |
| `http://localhost:8080/v3/api-docs.yaml` | Raw OpenAPI 3.0 YAML spec                   |

### 15.2 Documentation Standards

Every controller method must include:

```java
@Operation(
    summary = "Get movie details",
    description = "Retrieves full movie details including genres and average rating"
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Movie found"),
    @ApiResponse(responseCode = "404", description = "Movie not found")
})
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<MovieDetailResponse>> getMovie(@PathVariable Long id) { ... }
```

### 15.3 Swagger Security Configuration

The Swagger UI includes a "Authorize" button that accepts a Bearer token, allowing developers to test authenticated endpoints directly from the browser:

```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Movie Recommendation API")
                .version("1.0")
                .description("RESTful API for the Movie Recommendation System"))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
            .components(new Components()
                .addSecuritySchemes("Bearer Auth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

---

## 16. Performance Requirements & SLAs

| Metric                     | Target           | Measurement Method                    |
|----------------------------|------------------|---------------------------------------|
| Movie listing (cached)     | < 200ms          | Redis cache hit                       |
| Movie listing (uncached)   | < 1s             | PostgreSQL query + cache write        |
| Movie search               | < 500ms          | Trigram index + Redis cache           |
| Trending movies            | < 100ms          | Redis cache hit                       |
| Authentication (login)     | < 500ms          | BCrypt verify + token generation      |
| Review submission          | < 1s             | Write + avg rating recalculation      |
| Recommendation retrieval   | < 200ms          | Redis cache hit on pre-computed data  |
| Recommendation cron job    | < 10 min         | Async batch processing                |
| WebSocket event delivery   | < 500ms          | End-to-end from trigger to client     |
| API availability           | 99.5%            | Health check endpoint monitoring      |

---

## 17. Milestones & Delivery Timeline

| Phase | Milestone                          | Duration   | Deliverables                                                          |
|-------|------------------------------------|------------|-----------------------------------------------------------------------|
| 1     | **Foundation & Auth**              | Week 1-2   | Project scaffold, Flyway migrations, JWT auth, RBAC, Swagger setup    |
| 2     | **Core CRUD**                      | Week 3-4   | Movie & Genre CRUD, Review system (with likes/replies), Watchlist     |
| 3     | **Caching & Search**               | Week 5     | Redis integration, trending endpoint, full-text search                |
| 4     | **Recommendation Engine**          | Week 6-7   | Collaborative filtering, content-based, cold start, cron job          |
| 5     | **Notifications & WebSocket**      | Week 8     | Real-time notification system, event-driven architecture              |
| 6     | **Testing & Hardening**            | Week 9-10  | Unit tests, integration tests, Postman collection, performance tuning |
| 7     | **Docker & Documentation**         | Week 11    | Docker Compose, deployment guide, final Swagger review, handoff       |

**All 7 phases completed.**

---

> **This proposal serves as the authoritative technical contract between the Backend and Frontend teams.** All API endpoints, request/response formats, and error codes documented here will be implemented exactly as specified. The Frontend team can begin development against this specification using the Swagger UI, the provided Postman collection, or the [Frontend API Reference](FRONTEND_API.md).
