package com.movie.recommendation.integration;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.modules.auth.dto.LoginRequest;
import com.movie.recommendation.modules.auth.dto.RegisterRequest;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.Role;
import com.movie.recommendation.modules.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.LinkedHashMap;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> postgres;
    static final GenericContainer<?> redis;

    static {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine");
        postgres.start();

        redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
        redis.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected UserRepository userRepository;

    @SuppressWarnings("rawtypes")
    protected String registerUser(String email, String username) {
        RegisterRequest request = RegisterRequest.builder()
                .email(email)
                .username(username)
                .password("Test@123!")
                .preferences(List.of("action"))
                .build();

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/register", request, ApiResponse.class);

        LinkedHashMap<?, ?> data = (LinkedHashMap<?, ?>) response.getBody().getData();
        return (String) data.get("accessToken");
    }

    @SuppressWarnings("rawtypes")
    protected String loginAndGetToken(String email) {
        LoginRequest loginReq = LoginRequest.builder()
                .email(email)
                .password("Test@123!")
                .build();

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login", loginReq, ApiResponse.class);

        LinkedHashMap<?, ?> data = (LinkedHashMap<?, ?>) response.getBody().getData();
        return (String) data.get("accessToken");
    }

    protected String registerAdmin(String email, String username) {
        String token = registerUser(email, username);
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        return loginAndGetToken(email);
    }

    protected HttpHeaders authenticatedHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }
}
