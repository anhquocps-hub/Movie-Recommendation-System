package com.movie.recommendation.integration;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.modules.auth.dto.LoginRequest;
import com.movie.recommendation.modules.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends BaseIntegrationTest {

    private String uid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

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
        String id = uid();
        RegisterRequest request = createRegisterRequest("reg" + id + "@test.com", "reg" + id);

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
        String id = uid();
        RegisterRequest request1 = createRegisterRequest("dup" + id + "@test.com", "dup1" + id);
        restTemplate.postForEntity("/api/v1/auth/register", request1, ApiResponse.class);

        RegisterRequest request2 = createRegisterRequest("dup" + id + "@test.com", "dup2" + id);
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/register", request2, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void login_success_returns200() {
        String id = uid();
        RegisterRequest registerReq = createRegisterRequest("login" + id + "@test.com", "login" + id);
        restTemplate.postForEntity("/api/v1/auth/register", registerReq, ApiResponse.class);

        LoginRequest loginReq = LoginRequest.builder()
                .email("login" + id + "@test.com")
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
        String id = uid();
        RegisterRequest registerReq = createRegisterRequest("wrong" + id + "@test.com", "wrong" + id);
        restTemplate.postForEntity("/api/v1/auth/register", registerReq, ApiResponse.class);

        LoginRequest loginReq = LoginRequest.builder()
                .email("wrong" + id + "@test.com")
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
