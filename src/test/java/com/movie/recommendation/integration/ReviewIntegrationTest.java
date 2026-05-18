package com.movie.recommendation.integration;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.modules.movie.dto.CreateGenreRequest;
import com.movie.recommendation.modules.movie.dto.CreateMovieRequest;
import com.movie.recommendation.modules.review.dto.CreateReplyRequest;
import com.movie.recommendation.modules.review.dto.CreateReviewRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewIntegrationTest extends BaseIntegrationTest {

    private String adminToken;
    private String userToken;
    private Long movieId;
    private String uid;

    @BeforeEach
    @SuppressWarnings("rawtypes")
    void setUp() {
        uid = UUID.randomUUID().toString().substring(0, 8);
        adminToken = registerAdmin("revadm" + uid + "@test.com", "revadm" + uid);
        userToken = registerUser("revusr" + uid + "@test.com", "revusr" + uid);

        CreateGenreRequest genreReq = new CreateGenreRequest();
        genreReq.setName("Drama-" + uid);
        HttpEntity<CreateGenreRequest> genreEntity = new HttpEntity<>(genreReq, authenticatedHeaders(adminToken));
        ResponseEntity<ApiResponse> genreResp = restTemplate.exchange(
                "/api/v1/genres", HttpMethod.POST, genreEntity, ApiResponse.class);
        LinkedHashMap<?, ?> genreData = (LinkedHashMap<?, ?>) genreResp.getBody().getData();
        Integer genreId = (Integer) genreData.get("id");

        CreateMovieRequest movieReq = new CreateMovieRequest();
        movieReq.setTitle("Review Movie " + uid);
        movieReq.setOverview("For review testing");
        movieReq.setReleaseDate(LocalDate.of(2024, 3, 1));
        movieReq.setRuntimeMinutes(90);
        movieReq.setGenreIds(Set.of(genreId));

        HttpEntity<CreateMovieRequest> movieEntity = new HttpEntity<>(movieReq, authenticatedHeaders(adminToken));
        ResponseEntity<ApiResponse> movieResp = restTemplate.exchange(
                "/api/v1/movies", HttpMethod.POST, movieEntity, ApiResponse.class);
        LinkedHashMap<?, ?> movieData = (LinkedHashMap<?, ?>) movieResp.getBody().getData();
        movieId = ((Number) movieData.get("id")).longValue();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void createReview_returns201() {
        CreateReviewRequest req = new CreateReviewRequest();
        req.setRating((short) 4);
        req.setContent("Great film!");

        HttpEntity<CreateReviewRequest> entity = new HttpEntity<>(req, authenticatedHeaders(userToken));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/movies/" + movieId + "/reviews", HttpMethod.POST, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        LinkedHashMap<?, ?> data = (LinkedHashMap<?, ?>) response.getBody().getData();
        assertThat(data.get("rating")).isEqualTo(4);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void createReview_duplicate_returns409() {
        CreateReviewRequest req = new CreateReviewRequest();
        req.setRating((short) 5);
        req.setContent("First review");

        HttpEntity<CreateReviewRequest> entity = new HttpEntity<>(req, authenticatedHeaders(userToken));
        restTemplate.exchange("/api/v1/movies/" + movieId + "/reviews", HttpMethod.POST, entity, ApiResponse.class);

        req.setContent("Second attempt");
        entity = new HttpEntity<>(req, authenticatedHeaders(userToken));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/movies/" + movieId + "/reviews", HttpMethod.POST, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getMovieReviews_public_returns200() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/movies/" + movieId + "/reviews", ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void toggleLike_returns200() {
        CreateReviewRequest req = new CreateReviewRequest();
        req.setRating((short) 3);
        req.setContent("Decent");

        HttpEntity<CreateReviewRequest> reviewEntity = new HttpEntity<>(req, authenticatedHeaders(userToken));
        ResponseEntity<ApiResponse> reviewResp = restTemplate.exchange(
                "/api/v1/movies/" + movieId + "/reviews", HttpMethod.POST, reviewEntity, ApiResponse.class);
        LinkedHashMap<?, ?> reviewData = (LinkedHashMap<?, ?>) reviewResp.getBody().getData();
        Long reviewId = ((Number) reviewData.get("id")).longValue();

        String likerUid = UUID.randomUUID().toString().substring(0, 8);
        String otherToken = registerUser("liker" + likerUid + "@test.com", "liker" + likerUid);
        HttpEntity<Void> entity = new HttpEntity<>(authenticatedHeaders(otherToken));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/reviews/" + reviewId + "/like", HttpMethod.POST, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void createReply_returns201() {
        CreateReviewRequest reviewReq = new CreateReviewRequest();
        reviewReq.setRating((short) 4);
        reviewReq.setContent("Nice movie");

        HttpEntity<CreateReviewRequest> reviewEntity = new HttpEntity<>(reviewReq, authenticatedHeaders(userToken));
        ResponseEntity<ApiResponse> reviewResp = restTemplate.exchange(
                "/api/v1/movies/" + movieId + "/reviews", HttpMethod.POST, reviewEntity, ApiResponse.class);
        LinkedHashMap<?, ?> reviewData = (LinkedHashMap<?, ?>) reviewResp.getBody().getData();
        Long reviewId = ((Number) reviewData.get("id")).longValue();

        CreateReplyRequest replyReq = new CreateReplyRequest();
        replyReq.setContent("Thanks for the review!");

        String replierUid = UUID.randomUUID().toString().substring(0, 8);
        String replierToken = registerUser("rep" + replierUid + "@test.com", "rep" + replierUid);
        HttpEntity<CreateReplyRequest> replyEntity = new HttpEntity<>(replyReq, authenticatedHeaders(replierToken));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/reviews/" + reviewId + "/replies", HttpMethod.POST, replyEntity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getReplies_public_returns200() {
        CreateReviewRequest reviewReq = new CreateReviewRequest();
        reviewReq.setRating((short) 5);
        reviewReq.setContent("Amazing");

        HttpEntity<CreateReviewRequest> reviewEntity = new HttpEntity<>(reviewReq, authenticatedHeaders(userToken));
        ResponseEntity<ApiResponse> reviewResp = restTemplate.exchange(
                "/api/v1/movies/" + movieId + "/reviews", HttpMethod.POST, reviewEntity, ApiResponse.class);
        LinkedHashMap<?, ?> reviewData = (LinkedHashMap<?, ?>) reviewResp.getBody().getData();
        Long reviewId = ((Number) reviewData.get("id")).longValue();

        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/reviews/" + reviewId + "/replies", ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
