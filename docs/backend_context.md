## 1. Project Overview & Backend Role
The project is a web-based Movie Recommendation System designed to solve "information overload" by providing personalized movie suggestions [1, 2]. As the sole Backend Developer, my primary focus is to build a robust, scalable, and secure RESTful API system [3]. The backend must function independently, be fully testable without the UI, and provide clear API documentation (Swagger/OpenAPI) for the Frontend (Next.js) team to consume [4].

## 2. Technology Stack
*   **Core Framework:** Java with Spring Boot [4].
*   **Database:** PostgreSQL (Primary DB) [5].
*   **Database Migration:** Flyway (managing schema versions like V1__create.sql) [3, 6].
*   **Caching:** Redis (caching trending movies, search results with 1-hour TTL) [6, 7].
*   **Security & Auth:** Spring Security, JWT (Stateless authentication, 15-min Access Token, 7-day Refresh Token stored in HttpOnly Cookie), BCrypt password hashing [6, 8].
*   **Real-time Communication:** WebSocket (for instant notifications on likes/replies) [7, 9].
*   **Deployment & Containerization:** Docker & Docker Compose (packaging Backend, Postgres, Redis together) [3, 10].
*   **API Documentation:** Swagger / OpenAPI 3.0 (Crucial for frontend developers to know the API contracts).

## 3. Core Backend Modules & Features
1.  **Auth Module:** User registration, login, JWT token generation/refresh, and password reset [11].
2.  **User & RBAC Module:** Strict Role-Based Access Control (Admin, User, Guest) protecting specific endpoints (e.g., `@PreAuthorize("hasRole('ADMIN')")`) [10, 12].
3.  **Movie & Catalog Module:** CRUD operations for movies and genres. Implementing "Soft Delete" (`is_active = false`) to preserve historical data [10, 13].
4.  **Review & Interaction Module:** Handling ratings (1-5 stars), text reviews, likes, and replies. Ensuring data integrity (e.g., one review per user per movie) [13, 14].
5.  **Recommendation Module (Async/Cronjob):** A scheduled background task (Cronjob running every 6 hours) that calculates Collaborative Filtering algorithms and updates a `recommendations` table without blocking main threads [15, 16]. Handles "Cold Start" using user preferences [17].
6.  **Notification Module:** Pushing real-time events via WebSocket when users receive replies or likes [18, 19].

## 4. System Architecture & Design Patterns
*   **Architecture:** Layered Architecture (Controller -> Service -> Repository -> Entity) [19, 20]. 
    *   *Controller:* Handles HTTP requests, secured by JWT filters, delegates to Service [19].
    *   *Service:* Contains business logic, async processing, and external service calls [21].
    *   *Repository:* Spring Data JPA for PostgreSQL, `@Cacheable`/`@CacheEvict` for Redis [22].
*   **Design Patterns Applied:** 
    *   **DTO Pattern:** Strict separation between Entities and Request/Response DTOs. Standardized API response wrapper `{ status, message, data }` [16, 23, 24].
    *   **Repository Pattern:** Abstracting data access [25].
    *   **Observer Pattern:** Used in WebSocket event handling [23].
    *   **Strategy Pattern:** Switching between Collaborative Filtering and Content-based filtering algorithms [26].
    *   **Global Exception Handling:** `@ControllerAdvice` to catch all exceptions and return standardized JSON error formats [16].

## 5. Proposed Folder Architecture
To achieve the best scalability, the project follows a domain-driven / feature-based directory structure:
```text
src/main/java/com/movie/recommendation/
├── config/             # Swagger, Security, Redis, WebSocket configurations
├── exception/          # GlobalExceptionHandler, Custom Exceptions
├── security/           # JWT filters, UserDetails, AuthEntryPoint
├── common/             # BaseResponse, Pagination DTOs, Constants
├── modules/            # Feature-based packages
│   ├── auth/           # AuthController, AuthService, Auth DTOs
│   ├── user/           # UserEntity, UserRepository, UserService
│   ├── movie/          # MovieEntity, MovieController, MovieService, DTOs
│   ├── review/         # Review logic, Like/Reply handlers
│   ├── watchlist/      # Watchlist management
│   ├── recommendation/ # Cronjobs, AI Strategy patterns
│   └── notification/   # WebSocket handlers, Notification service
└── RecommendationApplication.java
```

## 6. Testing & Quality Assurance
Unit & Integration Testing: JUnit and Mockito to test Service layer business logic independent of the database.
API Testing: Using Postman collections and Swagger UI to verify that all endpoints return the correct HTTP status codes (200, 201, 400, 403, 404, 409) and the standardized JSON payload.
Performance Criteria: Core APIs must respond in < 3 seconds, Search in < 1 second, with Redis cache hits