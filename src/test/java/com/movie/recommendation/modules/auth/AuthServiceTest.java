package com.movie.recommendation.modules.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.recommendation.exception.DuplicateResourceException;
import com.movie.recommendation.modules.auth.dto.LoginRequest;
import com.movie.recommendation.modules.auth.dto.RegisterRequest;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.Role;
import com.movie.recommendation.modules.user.entity.User;
import com.movie.recommendation.security.CustomUserDetails;
import com.movie.recommendation.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private AuthService authService;

    @Test
    void register_success_returnsAuthResponse() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .username("testuser")
                .password("Test@123!")
                .preferences(List.of("action", "sci-fi"))
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtTokenProvider.generateAccessToken(any(CustomUserDetails.class)))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(CustomUserDetails.class)))
                .thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpirySeconds()).thenReturn(900L);

        AuthService.AuthTokens result = authService.register(request);

        assertThat(result.response().getAccessToken()).isEqualTo("access-token");
        assertThat(result.response().getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(result.response().getUser().getRole()).isEqualTo("USER");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .username("newuser")
                .password("Test@123!")
                .build();

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");
    }

    @Test
    void register_duplicateUsername_throwsDuplicateResourceException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .username("existinguser")
                .password("Test@123!")
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("username");
    }

    @Test
    void login_validCredentials_returnsAuthResponse() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("Test@123!")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .passwordHash("encoded")
                .role(Role.USER)
                .isActive(true)
                .build();

        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpirySeconds()).thenReturn(900L);

        AuthService.AuthTokens result = authService.login(request);

        assertThat(result.response().getAccessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
    }
}
