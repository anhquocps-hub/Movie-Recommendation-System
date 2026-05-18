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

class MovieIntegrationTest extends BaseIntegrationTest {

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        adminToken = registerAdmin("movadm" + suffix + "@test.com", "movadm" + suffix);
        userToken = registerUser("movusr" + suffix + "@test.com", "movusr" + suffix);
    }

    private Integer createGenre(String name) {
        CreateGenreRequest req = new CreateGenreRequest();
        req.setName(name);

        HttpEntity<CreateGenreRequest> entity = new HttpEntity<>(req, authenticatedHeaders(adminToken));
        @SuppressWarnings("rawtypes")
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/genres", HttpMethod.POST, entity, ApiResponse.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            LinkedHashMap<?, ?> data = (LinkedHashMap<?, ?>) response.getBody().getData();
            return (Integer) data.get("id");
        }
        return null;
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getGenres_public_returns200() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/genres", ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void createGenre_asUser_returnsForbidden() {
        CreateGenreRequest req = new CreateGenreRequest();
        req.setName("UserGenre-" + UUID.randomUUID().toString().substring(0, 6));

        HttpEntity<CreateGenreRequest> entity = new HttpEntity<>(req, authenticatedHeaders(userToken));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/genres", HttpMethod.POST, entity, ApiResponse.class);

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getMovies_public_returns200() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/movies", ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void createMovie_asAdmin_returns201() {
        String uid = UUID.randomUUID().toString().substring(0, 6);
        Integer genreId = createGenre("Action-" + uid);
        assertThat(genreId).isNotNull();

        CreateMovieRequest req = new CreateMovieRequest();
        req.setTitle("IT Movie " + uid);
        req.setOverview("A test movie");
        req.setReleaseDate(LocalDate.of(2024, 1, 1));
        req.setRuntimeMinutes(120);
        req.setGenreIds(Set.of(genreId));

        HttpEntity<CreateMovieRequest> entity = new HttpEntity<>(req, authenticatedHeaders(adminToken));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/movies", HttpMethod.POST, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        LinkedHashMap<?, ?> data = (LinkedHashMap<?, ?>) response.getBody().getData();
        assertThat((String) data.get("title")).startsWith("IT Movie");
    }

    @Test
    @SuppressWarnings("rawtypes")
    void createMovie_asUser_returnsForbidden() {
        CreateMovieRequest req = new CreateMovieRequest();
        req.setTitle("Forbidden Movie");
        req.setOverview("Should fail");
        req.setGenreIds(Set.of(1));

        HttpEntity<CreateMovieRequest> entity = new HttpEntity<>(req, authenticatedHeaders(userToken));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/movies", HttpMethod.POST, entity, ApiResponse.class);

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void searchMovies_public_returns200() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/movies/search?query=test", ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getTrending_public_returns200() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/movies/trending", ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getMovieDetail_notFound_returns404() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/movies/999999", ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void filterMovies_byGenre_returns200() {
        String uid = UUID.randomUUID().toString().substring(0, 6);
        Integer genreId = createGenre("Filter-" + uid);
        assertThat(genreId).isNotNull();

        CreateMovieRequest req = new CreateMovieRequest();
        req.setTitle("Filter Movie " + uid);
        req.setOverview("Filterable");
        req.setReleaseDate(LocalDate.of(2024, 1, 1));
        req.setRuntimeMinutes(90);
        req.setGenreIds(Set.of(genreId));

        HttpEntity<CreateMovieRequest> entity = new HttpEntity<>(req, authenticatedHeaders(adminToken));
        restTemplate.exchange("/api/v1/movies", HttpMethod.POST, entity, ApiResponse.class);

        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/movies?genreId=" + genreId, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void filterMovies_byYear_returns200() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/movies?year=2024", ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void searchMovies_withGenreFilter_returns200() {
        String uid = UUID.randomUUID().toString().substring(0, 6);
        Integer genreId = createGenre("SearchG-" + uid);
        assertThat(genreId).isNotNull();

        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/movies/search?query=test&genreId=" + genreId, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
