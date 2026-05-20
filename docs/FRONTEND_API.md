# Frontend Developer API Documentation

**Movie Recommendation System REST API**

Base URL: `http://localhost:8080/api/v1`  
API Version: v1  
Authentication: JWT Bearer Token

---

## Quick Start for Frontend Developers

### Prerequisites

- **Docker** and **Docker Compose**
- **Make**

### Running the Backend (Single Command)

```bash
make run-dev
```

This command:
- Starts PostgreSQL and Redis in Docker containers
- Runs the Spring Boot application locally with the `dev` profile
- Enables hot reload for backend development
- Takes ~120-150 seconds to start (Gradle will show `83% EXECUTING` — this is normal)

The API will be available at `http://localhost:8080/api/v1` and Swagger UI at `http://localhost:8080/swagger-ui.html`.

**To stop:** Press `Ctrl+C` in the terminal.

### Alternative: Full Docker Setup

If you prefer to run everything in Docker:

```bash
make run
```

This builds and starts the entire stack (app + Postgres + Redis) in Docker containers.

### Other Useful Commands

| Command               | Description                    |
|-----------------------|--------------------------------|
| `make docker-down`    | Stop and remove all containers |
| `make logs`           | View backend logs              |
| `make help`           | Show all available commands    |

For complete setup instructions, see the [main README](../README.md).

---

## Table of Contents

1. [Quick Start for Frontend Developers](#quick-start-for-frontend-developers)
2. [Overview](#overview)
3. [Authentication Flow](#authentication-flow)
4. [REST Endpoints](#rest-endpoints)
5. [Request/Response DTOs](#requestresponse-dtos)
6. [Pagination](#pagination)
7. [Error Handling](#error-handling)
8. [WebSocket Integration](#websocket-integration)
9. [Rate Limiting](#rate-limiting)
10. [CORS Configuration](#cors-configuration)

---

## Overview

### Base Configuration

- **Base URL**: `http://localhost:8080/api/v1`
- **Content-Type**: `application/json`
- **Authentication**: JWT Bearer Token in `Authorization` header
- **Refresh Token**: HttpOnly cookie named `refreshToken`

### Response Wrapper

All API responses follow this structure:

```json
{
  "status": 200,
  "message": "Success message",
  "data": { /* response data */ }
}
```

---

## Authentication Flow

### JWT Token Management

1. **Register/Login**: Receive `accessToken` in response body and `refreshToken` in HttpOnly cookie
2. **Access Token**: Include in every authenticated request: `Authorization: Bearer {accessToken}`
3. **Token Expiry**: Access token expires in 15 minutes (900,000ms)
4. **Refresh Token**: Expires in 7 days (604,800,000ms), stored in HttpOnly cookie
5. **Token Refresh**: POST `/auth/refresh` (cookie sent automatically by browser)
6. **Logout**: POST `/auth/logout` to invalidate refresh token

### Refresh Token Cookie Details

- **Name**: `refreshToken`
- **HttpOnly**: `true` (not accessible via JavaScript)
- **Secure**: `false` (development mode)
- **SameSite**: `Strict`
- **Path**: `/api/v1/auth`
- **Max-Age**: 7 days

### Authorization Roles

- **Public**: No authentication required
- **USER**: Default role for registered users
- **ADMIN**: Required for movie/genre management

---

## REST Endpoints

### Authentication

**Base Path**: `/api/v1/auth`

| Endpoint           | Method | Auth | Description                         |
|--------------------|--------|------|-------------------------------------|
| `/register`        | POST   | No   | Register new user                   |
| `/login`           | POST   | No   | Authenticate user                   |
| `/refresh`         | POST   | No   | Refresh access token (uses cookie)  |
| `/logout`          | POST   | No   | Logout and invalidate refresh token |
| `/forgot-password` | POST   | No   | Request password reset link         |
| `/reset-password`  | POST   | No   | Reset password with token           |

### Movies

**Base Path**: `/api/v1/movies`

| Endpoint        | Method | Auth  | Description                         |
|-----------------|--------|-------|-------------------------------------|
| `/`             | GET    | No    | List movies (paginated, filterable) |
| `/{id}`         | GET    | No    | Get movie details                   |
| `/search`       | GET    | No    | Search movies by title              |
| `/trending`     | GET    | No    | Get trending movies                 |
| `/`             | POST   | ADMIN | Create new movie                    |
| `/{id}`         | PUT    | ADMIN | Update movie details                |
| `/{id}`         | DELETE | ADMIN | Soft-delete movie                   |
| `/{id}/restore` | PATCH  | ADMIN | Restore soft-deleted movie          |

### Genres

**Base Path**: `/api/v1/genres`

| Endpoint | Method | Auth  | Description     |
|----------|--------|-------|-----------------|
| `/`      | GET    | No    | List all genres |
| `/`      | POST   | ADMIN | Create genre    |
| `/{id}`  | PUT    | ADMIN | Update genre    |
| `/{id}`  | DELETE | ADMIN | Delete genre    |

### Reviews

**Base Path**: `/api/v1`

| Endpoint                    | Method | Auth       | Description                        |
|-----------------------------|--------|------------|------------------------------------|
| `/movies/{movieId}/reviews` | GET    | No         | List reviews for movie (paginated) |
| `/movies/{movieId}/reviews` | POST   | USER       | Submit review                      |
| `/reviews/{id}`             | PUT    | USER       | Edit own review                    |
| `/reviews/{id}`             | DELETE | USER/ADMIN | Delete review                      |
| `/reviews/{id}/like`        | POST   | USER       | Toggle like on review              |
| `/reviews/{id}/replies`     | GET    | No         | List replies for review            |
| `/reviews/{id}/replies`     | POST   | USER       | Reply to review                    |

### Users

**Base Path**: `/api/v1/users`

| Endpoint           | Method | Auth  | Description                 |
|--------------------|--------|-------|-----------------------------|
| `/me`              | GET    | USER  | Get current user profile    |
| `/me`              | PUT    | USER  | Update current user profile |
| `/me/preferences`  | PUT    | USER  | Update genre preferences    |
| `/`                | GET    | ADMIN | Get all users (paginated)   |
| `/{id}`            | GET    | ADMIN | Get user by ID              |
| `/{id}/role`       | PATCH  | ADMIN | Change user role            |
| `/{id}`            | DELETE | ADMIN | Deactivate user             |

### Watchlist

**Base Path**: `/api/v1/watchlist`

| Endpoint       | Method | Auth | Description                      |
|----------------|--------|------|----------------------------------|
| `/`            | GET    | USER | Get user's watchlist (paginated) |
| `/{movieId}`   | POST   | USER | Add movie to watchlist           |
| `/{movieId}`   | DELETE | USER | Remove movie from watchlist      |

### Recommendations

**Base Path**: `/api/v1/recommendations`

| Endpoint     | Method | Auth  | Description                                   |
|--------------|--------|-------|-----------------------------------------------|
| `/`          | GET    | USER  | Get personalized recommendations (paginated)  |
| `/refresh`   | POST   | ADMIN | Trigger recommendation generation             |

### Notifications

**Base Path**: `/api/v1/notifications`

| Endpoint         | Method | Auth | Description                           |
|------------------|--------|------|---------------------------------------|
| `/`              | GET    | USER | List user's notifications (paginated) |
| `/{id}/read`     | PATCH  | USER | Mark notification as read             |
| `/read-all`      | PATCH  | USER | Mark all notifications as read        |
| `/unread-count`  | GET    | USER | Get unread notification count         |

---

## Request/Response DTOs

### Authentication DTOs

#### RegisterRequest

**POST** `/auth/register`

```json
{
  "email": "user@example.com",
  "username": "john_doe",
  "password": "SecurePass123!",
  "preferences": ["Action", "Sci-Fi"]
}
```

**Validations:**
- `email`: Required, valid email format, max 255 chars
- `username`: Required, 3-50 chars
- `password`: Required, 8-100 chars, must contain uppercase, lowercase, digit, special char (@$!%*?&)
- `preferences`: Optional list of genre names

#### LoginRequest

**POST** `/auth/login`

```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Validations:**
- `email`: Required, valid email format
- `password`: Required

#### AuthResponse

**Response from** `/auth/register`, `/auth/login`, `/auth/refresh`

```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "username": "john_doe",
      "role": "USER"
    }
  }
}
```

#### ForgotPasswordRequest

**POST** `/auth/forgot-password`

```json
{
  "email": "user@example.com"
}
```

**Validations:**
- `email`: Required, valid email format

#### ResetPasswordRequest

**POST** `/auth/reset-password`

```json
{
  "token": "reset-token-from-email",
  "newPassword": "NewSecurePass123!"
}
```

**Validations:**
- `token`: Required
- `newPassword`: Required, 8-100 chars, must contain uppercase, lowercase, digit, special char

---

### Movie DTOs

#### CreateMovieRequest

**POST** `/movies`

```json
{
  "title": "Inception",
  "overview": "A skilled thief who steals corporate secrets...",
  "posterUrl": "https://example.com/poster.jpg",
  "backdropUrl": "https://example.com/backdrop.jpg",
  "releaseDate": "2010-07-16",
  "runtimeMinutes": 148,
  "genreIds": [1, 2, 3]
}
```

**Validations:**
- `title`: Required, max 500 chars
- `overview`: Optional
- `posterUrl`: Optional, max 500 chars
- `backdropUrl`: Optional, max 500 chars
- `releaseDate`: Optional, ISO date format (YYYY-MM-DD)
- `runtimeMinutes`: Optional, min 1
- `genreIds`: Required, at least one genre ID

#### UpdateMovieRequest

**PUT** `/movies/{id}`

Same structure as `CreateMovieRequest`

#### MovieResponse

**GET** `/movies` (list endpoint)

```json
{
  "status": 200,
  "message": "Movies retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Inception",
        "slug": "inception",
        "posterUrl": "https://example.com/poster.jpg",
        "releaseDate": "2010-07-16",
        "avgRating": 8.8,
        "voteCount": 2500,
        "genres": ["Action", "Sci-Fi", "Thriller"]
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "last": false
  }
}
```

#### MovieDetailResponse

**GET** `/movies/{id}`

```json
{
  "status": 200,
  "message": "Movie retrieved successfully",
  "data": {
    "id": 1,
    "title": "Inception",
    "slug": "inception",
    "overview": "A skilled thief who steals corporate secrets...",
    "posterUrl": "https://example.com/poster.jpg",
    "backdropUrl": "https://example.com/backdrop.jpg",
    "releaseDate": "2010-07-16",
    "runtimeMinutes": 148,
    "avgRating": 8.8,
    "voteCount": 2500,
    "isActive": true,
    "genres": [
      {"id": 1, "name": "Action", "slug": "action"},
      {"id": 2, "name": "Sci-Fi", "slug": "sci-fi"}
    ],
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

---

### Genre DTOs

#### CreateGenreRequest

**POST** `/genres`

```json
{
  "name": "Action"
}
```

**Validations:**
- `name`: Required, max 50 chars

#### UpdateGenreRequest

**PUT** `/genres/{id}`

```json
{
  "name": "Adventure"
}
```

**Validations:**
- `name`: Required, max 50 chars

#### GenreResponse

**GET** `/genres`

```json
{
  "status": 200,
  "message": "Genres retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Action",
      "slug": "action"
    },
    {
      "id": 2,
      "name": "Sci-Fi",
      "slug": "sci-fi"
    }
  ]
}
```

---

### Review DTOs

#### CreateReviewRequest

**POST** `/movies/{movieId}/reviews`

```json
{
  "rating": 5,
  "content": "Amazing movie! Highly recommended.",
  "isSpoiler": false
}
```

**Validations:**
- `rating`: Required, integer 1-5
- `content`: Optional
- `isSpoiler`: Optional, default `false`

**Note:** Users can only submit one review per movie.

#### UpdateReviewRequest

**PUT** `/reviews/{id}`

```json
{
  "rating": 4,
  "content": "Updated review content",
  "isSpoiler": false
}
```

**Validations:**
- `rating`: Required, integer 1-5
- `content`: Optional
- `isSpoiler`: Optional

#### ReviewResponse

**GET** `/movies/{movieId}/reviews`

```json
{
  "status": 200,
  "message": "Reviews retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 5,
        "username": "john_doe",
        "movieId": 1,
        "movieTitle": "Inception",
        "rating": 5,
        "content": "Amazing movie!",
        "isSpoiler": false,
        "likeCount": 42,
        "replyCount": 3,
        "likedByCurrentUser": true,
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3,
    "last": false
  }
}
```

**Note:** `likedByCurrentUser` is `null` for unauthenticated requests.

#### CreateReplyRequest

**POST** `/reviews/{id}/replies`

```json
{
  "content": "I completely agree!"
}
```

**Validations:**
- `content`: Required, max 5000 chars

#### ReplyResponse

**GET** `/reviews/{id}/replies`

```json
{
  "status": 200,
  "message": "Replies retrieved successfully",
  "data": [
    {
      "id": 1,
      "userId": 6,
      "username": "jane_doe",
      "content": "I completely agree!",
      "createdAt": "2024-01-15T11:00:00",
      "updatedAt": "2024-01-15T11:00:00"
    }
  ]
}
```

---

### User DTOs

#### UpdateProfileRequest

**PUT** `/users/me`

```json
{
  "username": "new_username",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

**Validations:**
- `username`: Optional, 3-50 chars
- `avatarUrl`: Optional, max 500 chars

#### UpdatePreferencesRequest

**PUT** `/users/me/preferences`

```json
{
  "preferences": ["Action", "Sci-Fi", "Drama"]
}
```

**Validations:**
- `preferences`: Required, list of genre names

#### UpdateRoleRequest

**PATCH** `/users/{id}/role`

```json
{
  "role": "ADMIN"
}
```

**Validations:**
- `role`: Required, valid values: `USER`, `ADMIN`

#### UserProfileResponse

**GET** `/users/me`

```json
{
  "status": 200,
  "message": "User profile retrieved successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "username": "john_doe",
    "role": "USER",
    "avatarUrl": "https://example.com/avatar.jpg",
    "preferences": ["Action", "Sci-Fi"],
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

---

### Watchlist DTOs

#### WatchlistResponse

**GET** `/watchlist`

```json
{
  "status": 200,
  "message": "Watchlist retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "movieId": 1,
        "movieTitle": "Inception",
        "movieSlug": "inception",
        "posterUrl": "https://example.com/poster.jpg",
        "avgRating": 8.8,
        "addedAt": "2024-01-15T10:30:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1,
    "last": true
  }
}
```

---

### Recommendation DTOs

#### RecommendationResponse

**GET** `/recommendations`

```json
{
  "status": 200,
  "message": "Recommendations retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "score": 0.95,
        "strategyType": "COLLABORATIVE_FILTERING",
        "generatedAt": "2024-01-15T10:30:00",
        "movie": {
          "id": 1,
          "title": "Inception",
          "slug": "inception",
          "posterUrl": "https://example.com/poster.jpg",
          "releaseDate": "2010-07-16",
          "avgRating": 8.8,
          "voteCount": 2500,
          "genres": ["Action", "Sci-Fi"]
        }
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3,
    "last": false
  }
}
```

**Strategy Types:**
- `COLLABORATIVE_FILTERING`: Based on similar users' preferences
- `CONTENT_BASED`: Based on movie attributes and user preferences

---

### Notification DTOs

#### NotificationResponse

**GET** `/notifications`

```json
{
  "status": 200,
  "message": "Notifications retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "type": "REVIEW_REPLY",
        "actorId": 5,
        "actorName": "john_doe",
        "referenceId": 10,
        "message": "john_doe replied to your review",
        "read": false,
        "createdAt": "2024-01-15T10:30:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 10,
    "totalPages": 1,
    "last": true
  }
}
```

**Notification Types:**
- `REVIEW_REPLY`: Someone replied to your review
- `REVIEW_LIKE`: Someone liked your review

#### UnreadCountResponse

**GET** `/notifications/unread-count`

```json
{
  "status": 200,
  "message": "Unread count retrieved successfully",
  "data": 5
}
```

---

## Pagination

### Query Parameters

All list endpoints support pagination with these query parameters:

```
?page=0&size=20&sortBy=createdAt&sortDir=desc
```

| Parameter | Type   | Default     | Description                      |
|-----------|--------|-------------|----------------------------------|
| `page`    | int    | 0           | Zero-indexed page number         |
| `size`    | int    | 20          | Items per page (max 50)          |
| `sortBy`  | string | `createdAt` | Field to sort by                 |
| `sortDir` | string | `desc`      | Sort direction: `asc` or `desc`  |

### Movies Filtering

**GET** `/movies?page=0&size=20&genreId=1&year=2023&minRating=7.5`

| Parameter   | Type    | Description              |
|-------------|---------|--------------------------|
| `genreId`   | int     | Filter by genre ID       |
| `year`      | int     | Filter by release year   |
| `minRating` | decimal | Filter by minimum rating |

### Movies Search

**GET** `/movies/search?query=inception&genreId=1&page=0&size=20`

| Parameter | Type   | Required | Description             |
|-----------|--------|----------|-------------------------|
| `query`   | string | Yes      | Search term             |
| `genreId` | int    | No       | Filter results by genre |

### Paginated Response Format

```json
{
  "status": 200,
  "message": "Success message",
  "data": {
    "content": [ /* array of items */ ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "last": false
  }
}
```

| Field           | Type    | Description                              |
|-----------------|---------|------------------------------------------|
| `content`       | array   | Items for current page                   |
| `page`          | int     | Current page number (0-indexed)          |
| `size`          | int     | Items per page                           |
| `totalElements` | long    | Total number of items across all pages   |
| `totalPages`    | int     | Total number of pages                    |
| `last`          | boolean | `true` if this is the last page          |

---

## Error Handling

### Error Response Format

```json
{
  "status": 400,
  "message": "Validation failed",
  "data": {
    "email": "Must be a valid email address",
    "password": "Password must contain at least 1 uppercase, 1 lowercase, 1 digit, and 1 special character"
  }
}
```

For non-validation errors, `data` is `null`:

```json
{
  "status": 404,
  "message": "Movie not found with id: 999",
  "data": null
}
```

### Common Error Codes

| Status | Message                                                    | Scenario                     |
|--------|------------------------------------------------------------|------------------------------|
| 400    | Validation failed                                          | Invalid request body         |
| 400    | Malformed JSON request                                     | Invalid JSON syntax          |
| 401    | Invalid email or password                                  | Login failed                 |
| 401    | Refresh token is missing                                   | Token refresh without cookie |
| 401    | Invalid or expired reset token                             | Password reset token invalid |
| 403    | You do not have permission to access this resource         | Insufficient permissions     |
| 404    | {Resource} not found with {field}: {value}                 | Entity doesn't exist         |
| 409    | Email or username already exists                           | Duplicate registration       |
| 409    | Genre name already exists                                  | Duplicate genre              |
| 409    | Already reviewed this movie                                | Duplicate review             |
| 409    | Movie already in watchlist                                 | Duplicate watchlist entry    |
| 429    | Rate limit exceeded. Try again later.                      | Too many requests            |
| 500    | An unexpected error occurred                               | Server error                 |

---

## WebSocket Integration

### Connection Setup

**Endpoint**: `ws://localhost:8080/ws`  
**Protocol**: STOMP over WebSocket with SockJS fallback

### Authentication

Pass JWT token in the `Authorization` header during STOMP CONNECT:

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

const headers = {
  'Authorization': `Bearer ${accessToken}`
};

stompClient.connect(headers, (frame) => {
  console.log('Connected:', frame);
  
  // Subscribe to user-specific notifications
  stompClient.subscribe('/user/queue/notifications', (message) => {
    const notification = JSON.parse(message.body);
    console.log('New notification:', notification);
  });
});
```

### Message Broker Configuration

- **Simple Broker Destinations**: `/topic`, `/queue`
- **Application Prefix**: `/app`
- **User Destination Prefix**: `/user`

### Subscription Topics

#### User-Specific Notifications

**Topic**: `/user/queue/notifications`

Receives real-time notifications when:
- Someone replies to your review
- Someone likes your review

**Message Format**:

```json
{
  "id": 1,
  "type": "REVIEW_REPLY",
  "actorId": 5,
  "actorName": "john_doe",
  "referenceId": 10,
  "message": "john_doe replied to your review",
  "read": false,
  "createdAt": "2024-01-15T10:30:00"
}
```

### Example: React Integration

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const connectWebSocket = (accessToken, onNotification) => {
  const socket = new SockJS('http://localhost:8080/ws');
  const stompClient = Stomp.over(socket);

  stompClient.connect(
    { Authorization: `Bearer ${accessToken}` },
    () => {
      stompClient.subscribe('/user/queue/notifications', (message) => {
        const notification = JSON.parse(message.body);
        onNotification(notification);
      });
    },
    (error) => {
      console.error('WebSocket connection error:', error);
    }
  );

  return stompClient;
};
```

---

## Rate Limiting

### Configuration

- **Limit**: 100 requests per minute per client IP
- **Scope**: Per IP address (uses `X-Forwarded-For` header if present)

### Rate Limit Response

**HTTP 429 Too Many Requests**

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again later."
}
```

### Handling Rate Limits

Implement exponential backoff when receiving 429 responses:

```javascript
const fetchWithRetry = async (url, options, retries = 3) => {
  try {
    const response = await fetch(url, options);
    
    if (response.status === 429 && retries > 0) {
      const delay = Math.pow(2, 3 - retries) * 1000;
      await new Promise(resolve => setTimeout(resolve, delay));
      return fetchWithRetry(url, options, retries - 1);
    }
    
    return response;
  } catch (error) {
    throw error;
  }
};
```

---

## CORS Configuration

### Allowed Origins

Default: `http://localhost:3000`

Configure via environment variable: `CORS_ORIGINS`

### Allowed Methods

`GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`

### Allowed Headers

All headers (`*`)

### Credentials

Allowed (`true`) — required for cookie-based refresh tokens

### Max Age

3600 seconds (1 hour)

---

## Additional Notes

### Caching

The API uses Redis caching for:
- **Trending movies**: 1 hour TTL
- **Genre list**: 24 hours TTL
- **Movie details**: 1 hour TTL
- **Movie search results**: 30 minutes TTL
- **User recommendations**: 6 hours TTL
- **User notifications**: 5 minutes TTL

### Date/Time Format

All timestamps use ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`

Example: `2024-01-15T10:30:00`

### Validation Constraints Reference

| Annotation         | Usage                        | Example             |
|--------------------|------------------------------|---------------------|
| `@NotBlank`        | String must not be empty     | Email, password     |
| `@NotNull`         | Field must not be null       | Rating              |
| `@NotEmpty`        | Collection must not be empty | Genre IDs           |
| `@Email`           | Valid email format           | Email field         |
| `@Size(min, max)`  | String/collection length     | Username 3-50 chars |
| `@Min(value)`      | Numeric minimum              | Rating min 1        |
| `@Max(value)`      | Numeric maximum              | Rating max 5        |
| `@Pattern(regexp)` | Regex validation             | Password complexity |

### Password Requirements

- Minimum 8 characters, maximum 100 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 digit
- At least 1 special character from: `@$!%*?&`

---

## Support

For API issues or questions, refer to:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/v3/api-docs`
- **GitHub Issues**: [Project Repository](https://github.com/your-repo)

---

**Last Updated**: 2026-05-20  
**API Version**: v1
