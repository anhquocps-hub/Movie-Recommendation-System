package com.movie.recommendation.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
        filterChain = mock(FilterChain.class);
    }

    @Test
    void doFilterInternal_underLimit_allowsRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/movies");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void doFilterInternal_exceedsLimit_returns429() throws Exception {
        String clientIp = "10.0.0.1";

        for (int i = 0; i < 100; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/movies");
            req.setRemoteAddr(clientIp);
            MockHttpServletResponse resp = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(req, resp, filterChain);
        }

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/movies");
        request.setRemoteAddr(clientIp);
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).contains("Rate limit exceeded");
    }

    @Test
    void doFilterInternal_usesXForwardedForHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/movies");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.50, 70.41.3.18");
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_differentIps_haveSeparateBuckets() throws Exception {
        for (int i = 0; i < 100; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/movies");
            req.setRemoteAddr("10.0.0.100");
            MockHttpServletResponse resp = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(req, resp, filterChain);
        }

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/movies");
        request.setRemoteAddr("10.0.0.200");
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(101)).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void doFilterInternal_rateLimitResponse_hasJsonContentType() throws Exception {
        String clientIp = "10.0.0.50";
        for (int i = 0; i < 100; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/movies");
            req.setRemoteAddr(clientIp);
            rateLimitFilter.doFilterInternal(req, new MockHttpServletResponse(), filterChain);
        }

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/movies");
        request.setRemoteAddr(clientIp);
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString()).contains("\"status\":429");
    }
}
