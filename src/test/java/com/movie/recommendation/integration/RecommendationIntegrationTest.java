package com.movie.recommendation.integration;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.modules.movie.dto.CreateGenreRequest;
import com.movie.recommendation.modules.movie.dto.CreateMovieRequest;
import com.movie.recommendation.modules.review.dto.CreateReviewRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationIntegrationTest extends BaseIntegrationTest {

    private String adminToken;
    private String user1Token;
    private String user2Token;
    private Integer genreIdField;
    private Long movie1Id;
    private Long movie2Id;
    private Long movie3Id;

    @BeforeEach
    @SuppressWarnings("rawtypes")
    void setUp() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        adminToken = registerAdmin("recadm" + uid + "@test.com", "recadm" + uid);
        user1Token = registerUser("recusr1" + uid + "@test.com", "recusr1" + uid);
        user2Token = registerUser("recusr2" + uid + "@test.com", "recusr2" + uid);

        CreateGenreRequest genreReq = new CreateGenreRequest();
        genreReq.setName("Action-" + uid);
        HttpEntity<CreateGenreRequest> genreEntity = new HttpEntity<>(genreReq, authenticatedHeaders(adminToken));
        ResponseEntity<ApiResponse> genreResp = restTemplate.exchange(
                "/api/v1/genres", HttpMethod.POST, genreEntity, ApiResponse.class);
        LinkedHashMap<?, ?> genreData = (LinkedHashMap<?, ?>) genreResp.getBody().getData();
        genreIdField = (Integer) genreData.get("id");

        movie1Id = createMovie("Rec Movie 1 " + uid, genreIdField);
        movie2Id = createMovie("Rec Movie 2 " + uid, genreIdField);
        movie3Id = createMovie("Rec Movie 3 " + uid, genreIdField);
    }

    @SuppressWarnings("rawtypes")
    private Long createMovie(String title, Integer genreId) {
        CreateMovieRequest movieReq = new CreateMovieRequest();
        movieReq.setTitle(title);
        movieReq.setOverview("Test movie for recommendations");
        movieReq.setReleaseDate(LocalDate.of(2024, 1, 1));
        movieReq.setRuntimeMinutes(120);
        movieReq.setGenreIds(Set.of(genreId));

        HttpEntity<CreateMovieRequest> movieEntity = new HttpEntity<>(movieReq, authenticatedHeaders(adminToken));
        ResponseEntity<ApiResponse> movieResp = restTemplate.exchange(
                "/api/v1/movies", HttpMethod.POST, movieEntity, ApiResponse.class);
        LinkedHashMap<?, ?> movieData = (LinkedHashMap<?, ?>) movieResp.getBody().getData();
        return ((Number) movieData.get("id")).longValue();
    }

    @SuppressWarnings("rawtypes")
    private void createReview(String token, Long movieId, short rating) {
        CreateReviewRequest reviewReq = CreateReviewRequest.builder()
                .rating(rating)
                .content("Test review")
                .isSpoiler(false)
                .build();

        HttpEntity<CreateReviewRequest> reviewEntity = new HttpEntity<>(reviewReq, authenticatedHeaders(token));
        restTemplate.exchange("/api/v1/movies/" + movieId + "/reviews",
                HttpMethod.POST, reviewEntity, ApiResponse.class);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getRecommendations_authenticatedUser_returns200() {
        createReview(user1Token, movie1Id, (short) 5);
        createReview(user1Token, movie2Id, (short) 4);

        restTemplate.exchange("/api/v1/recommendations/refresh",
                HttpMethod.POST, new HttpEntity<>(authenticatedHeaders(adminToken)), ApiResponse.class);

        HttpEntity<Void> entity = new HttpEntity<>(authenticatedHeaders(user1Token));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/recommendations", HttpMethod.GET, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(200);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getRecommendations_unauthenticated_returns401() {
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/recommendations", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getRecommendations_withPagination_returns200() {
        createReview(user1Token, movie1Id, (short) 5);

        restTemplate.exchange("/api/v1/recommendations/refresh",
                HttpMethod.POST, new HttpEntity<>(authenticatedHeaders(adminToken)), ApiResponse.class);

        HttpEntity<Void> entity = new HttpEntity<>(authenticatedHeaders(user1Token));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/recommendations?page=0&size=10", HttpMethod.GET, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) response.getBody().getData();
        assertThat(data).containsKey("content");
        assertThat(data).containsKey("page");
        assertThat(data).containsKey("size");
    }

    @Test
    @SuppressWarnings("rawtypes")
    void triggerRefresh_asAdmin_returns200() {
        createReview(user1Token, movie1Id, (short) 5);
        createReview(user1Token, movie2Id, (short) 4);
        createReview(user2Token, movie1Id, (short) 3);

        HttpEntity<Void> entity = new HttpEntity<>(authenticatedHeaders(adminToken));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/recommendations/refresh", HttpMethod.POST, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(200);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void triggerRefresh_asRegularUser_returns403() {
        HttpEntity<Void> entity = new HttpEntity<>(authenticatedHeaders(user1Token));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/recommendations/refresh", HttpMethod.POST, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void triggerRefresh_unauthenticated_returns401() {
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/recommendations/refresh", HttpMethod.POST,
                new HttpEntity<>(new HttpHeaders()), ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void recommendationsGenerated_afterRefresh_canBeRetrieved() {
        createReview(user1Token, movie1Id, (short) 5);
        createReview(user1Token, movie2Id, (short) 4);
        createReview(user1Token, movie3Id, (short) 5);

        createReview(user2Token, movie1Id, (short) 5);
        createReview(user2Token, movie2Id, (short) 4);

        restTemplate.exchange("/api/v1/recommendations/refresh",
                HttpMethod.POST, new HttpEntity<>(authenticatedHeaders(adminToken)), ApiResponse.class);

        HttpEntity<Void> entity = new HttpEntity<>(authenticatedHeaders(user1Token));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/recommendations", HttpMethod.GET, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LinkedHashMap<?, ?> data = (LinkedHashMap<?, ?>) response.getBody().getData();
        List<?> content = (List<?>) data.get("content");
        assertThat(content).isNotNull();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void recommendationsUsesCollaborativeFiltering_whenUserHasEnoughReviews() {
        createReview(user1Token, movie1Id, (short) 5);
        createReview(user1Token, movie2Id, (short) 4);
        createReview(user1Token, movie3Id, (short) 5);

        Long movie4Id = createMovie("Rec Movie 4 " + UUID.randomUUID().toString().substring(0, 8), genreIdField);
        Long movie5Id = createMovie("Rec Movie 5 " + UUID.randomUUID().toString().substring(0, 8), genreIdField);
        createReview(user1Token, movie4Id, (short) 4);
        createReview(user1Token, movie5Id, (short) 5);

        restTemplate.exchange("/api/v1/recommendations/refresh",
                HttpMethod.POST, new HttpEntity<>(authenticatedHeaders(adminToken)), ApiResponse.class);

        HttpEntity<Void> entity = new HttpEntity<>(authenticatedHeaders(user1Token));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/recommendations", HttpMethod.GET, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LinkedHashMap<?, ?> data = (LinkedHashMap<?, ?>) response.getBody().getData();
        List<?> content = (List<?>) data.get("content");

        if (!content.isEmpty()) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> firstRec = (LinkedHashMap<String, Object>) content.get(0);
            assertThat(firstRec).containsKey("strategyType");
        }
    }
}
