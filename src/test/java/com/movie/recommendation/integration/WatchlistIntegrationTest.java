package com.movie.recommendation.integration;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.modules.movie.dto.CreateGenreRequest;
import com.movie.recommendation.modules.movie.dto.CreateMovieRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WatchlistIntegrationTest extends BaseIntegrationTest {

    private String adminToken;
    private String userToken;
    private Long movieId;

    @BeforeEach
    @SuppressWarnings("rawtypes")
    void setUp() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        adminToken = registerAdmin("wladm" + uid + "@test.com", "wladm" + uid);
        userToken = registerUser("wlusr" + uid + "@test.com", "wlusr" + uid);

        CreateGenreRequest genreReq = new CreateGenreRequest();
        genreReq.setName("Thriller-" + uid);
        HttpEntity<CreateGenreRequest> genreEntity = new HttpEntity<>(genreReq, authenticatedHeaders(adminToken));
        ResponseEntity<ApiResponse> genreResp = restTemplate.exchange(
                "/api/v1/genres", HttpMethod.POST, genreEntity, ApiResponse.class);
        LinkedHashMap<?, ?> genreData = (LinkedHashMap<?, ?>) genreResp.getBody().getData();
        Integer genreId = (Integer) genreData.get("id");

        CreateMovieRequest movieReq = new CreateMovieRequest();
        movieReq.setTitle("WL Movie " + uid);
        movieReq.setOverview("For watchlist testing");
        movieReq.setReleaseDate(LocalDate.of(2024, 5, 1));
        movieReq.setRuntimeMinutes(110);
        movieReq.setGenreIds(Set.of(genreId));

        HttpEntity<CreateMovieRequest> movieEntity = new HttpEntity<>(movieReq, authenticatedHeaders(adminToken));
        ResponseEntity<ApiResponse> movieResp = restTemplate.exchange(
                "/api/v1/movies", HttpMethod.POST, movieEntity, ApiResponse.class);
        LinkedHashMap<?, ?> movieData = (LinkedHashMap<?, ?>) movieResp.getBody().getData();
        movieId = ((Number) movieData.get("id")).longValue();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void addToWatchlist_returns201() {
        HttpEntity<Void> entity = new HttpEntity<>(authenticatedHeaders(userToken));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/watchlist/" + movieId, HttpMethod.POST, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getWatchlist_returns200() {
        HttpEntity<Void> addEntity = new HttpEntity<>(authenticatedHeaders(userToken));
        restTemplate.exchange("/api/v1/watchlist/" + movieId, HttpMethod.POST, addEntity, ApiResponse.class);

        HttpEntity<Void> getEntity = new HttpEntity<>(authenticatedHeaders(userToken));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/watchlist", HttpMethod.GET, getEntity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void removeFromWatchlist_returns200() {
        HttpEntity<Void> addEntity = new HttpEntity<>(authenticatedHeaders(userToken));
        restTemplate.exchange("/api/v1/watchlist/" + movieId, HttpMethod.POST, addEntity, ApiResponse.class);

        HttpEntity<Void> deleteEntity = new HttpEntity<>(authenticatedHeaders(userToken));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/watchlist/" + movieId, HttpMethod.DELETE, deleteEntity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void addToWatchlist_duplicate_returns409() {
        HttpEntity<Void> entity = new HttpEntity<>(authenticatedHeaders(userToken));
        restTemplate.exchange("/api/v1/watchlist/" + movieId, HttpMethod.POST, entity, ApiResponse.class);

        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/watchlist/" + movieId, HttpMethod.POST, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
