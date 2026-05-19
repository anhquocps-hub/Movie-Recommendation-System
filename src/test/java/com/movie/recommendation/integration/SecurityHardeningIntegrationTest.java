package com.movie.recommendation.integration;

import com.movie.recommendation.common.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityHardeningIntegrationTest extends BaseIntegrationTest {

    private String uid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // --- Actuator Endpoints ---

    @Test
    @SuppressWarnings("rawtypes")
    void actuatorHealth_public_returns200() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @SuppressWarnings("rawtypes")
    void actuatorHealthLiveness_public_returns200() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/actuator/health/liveness", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void actuatorHealthReadiness_public_returns200() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/actuator/health/readiness", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void actuatorInfo_public_returns200() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/actuator/info", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void actuatorMetrics_withoutAuth_returns401() {
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/actuator/metrics", HttpMethod.GET, null, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void actuatorPrometheus_withoutAuth_returns401() {
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/actuator/prometheus", HttpMethod.GET, null, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // --- Security Headers ---

    @Test
    void securityHeaders_presentOnResponse() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/movies?page=0&size=1", String.class);

        HttpHeaders headers = response.getHeaders();
        assertThat(headers.getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(headers.getFirst("X-Frame-Options")).isEqualTo("DENY");
        assertThat(headers.getFirst("Cache-Control")).isNotNull();
    }

    // --- Request Logging / Trace ID ---

    @Test
    void xTraceIdHeader_presentOnApiResponse() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/movies?page=0&size=1", String.class);

        assertThat(response.getHeaders().getFirst("X-Trace-Id")).isNotNull();
        assertThat(response.getHeaders().getFirst("X-Trace-Id")).hasSize(16);
    }

    @Test
    void xTraceIdHeader_uniquePerRequest() {
        ResponseEntity<String> response1 = restTemplate.getForEntity(
                "/api/v1/movies?page=0&size=1", String.class);
        ResponseEntity<String> response2 = restTemplate.getForEntity(
                "/api/v1/movies?page=0&size=1", String.class);

        String traceId1 = response1.getHeaders().getFirst("X-Trace-Id");
        String traceId2 = response2.getHeaders().getFirst("X-Trace-Id");

        assertThat(traceId1).isNotEqualTo(traceId2);
    }
}
