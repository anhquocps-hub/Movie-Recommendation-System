package com.movie.recommendation.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RequestLoggingFilterTest {

    private RequestLoggingFilter loggingFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        loggingFilter = new RequestLoggingFilter();
        filterChain = mock(FilterChain.class);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void doFilterInternal_setsXTraceIdHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/movies");
        MockHttpServletResponse response = new MockHttpServletResponse();

        loggingFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("X-Trace-Id")).isNotNull();
        assertThat(response.getHeader("X-Trace-Id")).hasSize(16);
    }

    @Test
    void doFilterInternal_setsMdcDuringChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            assertThat(MDC.get("traceId")).isNotNull().hasSize(16);
            assertThat(MDC.get("method")).isEqualTo("POST");
            assertThat(MDC.get("uri")).isEqualTo("/api/v1/auth/login");
            return null;
        }).when(filterChain).doFilter(request, response);

        loggingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_clearsMdcAfterRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/movies");
        MockHttpServletResponse response = new MockHttpServletResponse();

        loggingFilter.doFilterInternal(request, response, filterChain);

        assertThat(MDC.get("traceId")).isNull();
        assertThat(MDC.get("method")).isNull();
        assertThat(MDC.get("uri")).isNull();
    }

    @Test
    void doFilterInternal_clearsMdcEvenOnException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/movies");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doThrow(new RuntimeException("test error")).when(filterChain).doFilter(request, response);

        try {
            loggingFilter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException ignored) {
        }

        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void shouldNotFilter_actuatorPaths_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        assertThat(loggingFilter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldNotFilter_webSocketPaths_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws/info");
        assertThat(loggingFilter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldNotFilter_apiPaths_returnsFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/movies");
        assertThat(loggingFilter.shouldNotFilter(request)).isFalse();
    }
}
