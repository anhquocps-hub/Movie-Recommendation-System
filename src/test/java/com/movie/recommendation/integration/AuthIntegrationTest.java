package com.movie.recommendation.integration;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.modules.auth.dto.LoginRequest;
import com.movie.recommendation.modules.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private TestRestTemplate restTemplate;

    private RegisterRequest createRegisterRequest(String email, String username) {
        return RegisterRequest.builder()
                .email(email)
                .username(username)
                .password("Test@123!")
                .preferences(List.of("action"))
                .build();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void register_success_returns201WithToken() {
        RegisterRequest request = createRegisterRequest("register1@test.com", "register1");

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/register", request, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(201);
        assertThat(response.getBody().getData()).isNotNull();

        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("refreshToken");
        assertThat(setCookie).contains("HttpOnly");
    }

    @Test
    @SuppressWarnings("rawtypes")
    void register_duplicateEmail_returns409() {
        RegisterRequest request1 = createRegisterRequest("dup@test.com", "dupuser1");
        restTemplate.postForEntity("/api/v1/auth/register", request1, ApiResponse.class);

        RegisterRequest request2 = createRegisterRequest("dup@test.com", "dupuser2");
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/register", request2, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void login_success_returns200() {
        RegisterRequest registerReq = createRegisterRequest("login@test.com", "loginuser");
        restTemplate.postForEntity("/api/v1/auth/register", registerReq, ApiResponse.class);

        LoginRequest loginReq = LoginRequest.builder()
                .email("login@test.com")
                .password("Test@123!")
                .build();

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login", loginReq, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void login_wrongPassword_returns401() {
        RegisterRequest registerReq = createRegisterRequest("wrong@test.com", "wronguser");
        restTemplate.postForEntity("/api/v1/auth/register", registerReq, ApiResponse.class);

        LoginRequest loginReq = LoginRequest.builder()
                .email("wrong@test.com")
                .password("WrongPassword1!")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> entity = new HttpEntity<>(loginReq, headers);

        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/auth/login", HttpMethod.POST, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void protectedEndpoint_withoutToken_returns401() {
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/users/me", HttpMethod.GET, null, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
