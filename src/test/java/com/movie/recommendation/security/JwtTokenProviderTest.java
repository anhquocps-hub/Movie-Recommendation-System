package com.movie.recommendation.security;

import com.movie.recommendation.modules.user.entity.Role;
import com.movie.recommendation.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "VGhpc0lzQVRlc3RTZWNyZXRLZXlUaGF0SXNBdExlYXN0NjRCeXRlc0xvbmdGb3JIUzUxMkFsZ29yaXRobVRlc3Qh");
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiry", 900000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiry", 604800000L);
        jwtTokenProvider.init();

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .passwordHash("hashed")
                .role(Role.USER)
                .isActive(true)
                .build();
        userDetails = CustomUserDetails.fromUser(user);
    }

    @Test
    void generateAccessToken_returnsValidJwt() {
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void getUserIdFromToken_returnsCorrectId() {
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(1L);
    }

    @Test
    void generateRefreshToken_hasJti() {
        String token = jwtTokenProvider.generateRefreshToken(userDetails);

        String jti = jwtTokenProvider.getJtiFromToken(token);

        assertThat(jti).isNotBlank();
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiry", 0L);
        jwtTokenProvider.init();

        String token = jwtTokenProvider.generateAccessToken(userDetails);

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_malformedToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("not.a.valid.token")).isFalse();
    }

    @Test
    void validateToken_nullToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }
}
