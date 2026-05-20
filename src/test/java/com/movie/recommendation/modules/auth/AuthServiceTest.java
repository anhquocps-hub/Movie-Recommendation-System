package com.movie.recommendation.modules.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.recommendation.exception.BadRequestException;
import com.movie.recommendation.exception.DuplicateResourceException;
import com.movie.recommendation.exception.UnauthorizedException;
import com.movie.recommendation.modules.auth.dto.*;
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
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
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

    private User buildUser() {
        return User.builder()
                .id(1L).email("test@example.com").username("testuser")
                .passwordHash("encoded").role(Role.USER).isActive(true)
                .build();
    }

    private void stubTokenGeneration() {
        when(jwtTokenProvider.generateAccessToken(any(CustomUserDetails.class)))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(CustomUserDetails.class)))
                .thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpirySeconds()).thenReturn(900L);
    }

    // --- register ---

    @Test
    void register_success_returnsAuthResponse() throws JsonProcessingException {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com").username("testuser")
                .password("Test@123!").preferences(List.of("action", "sci-fi"))
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(objectMapper.writeValueAsString(any())).thenReturn("[\"action\",\"sci-fi\"]");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        stubTokenGeneration();

        AuthService.AuthTokens result = authService.register(request);

        assertThat(result.response().getAccessToken()).isEqualTo("access-token");
        assertThat(result.response().getUser().getRole()).isEqualTo("USER");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void register_nullPreferences_usesEmptyArray() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com").username("testuser")
                .password("Test@123!").preferences(null)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        stubTokenGeneration();

        AuthService.AuthTokens result = authService.register(request);

        assertThat(result.response()).isNotNull();
    }

    @Test
    void register_jsonProcessingException_fallsBackToEmptyArray() throws JsonProcessingException {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com").username("testuser")
                .password("Test@123!").preferences(List.of("action"))
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("err") {});
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        stubTokenGeneration();

        AuthService.AuthTokens result = authService.register(request);

        assertThat(result.response()).isNotNull();
    }

    @Test
    void register_duplicateEmail_throws() {
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com").username("newuser").password("Test@123!")
                .build();
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class).hasMessageContaining("email");
    }

    @Test
    void register_duplicateUsername_throws() {
        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com").username("existinguser").password("Test@123!")
                .build();
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class).hasMessageContaining("username");
    }

    // --- login ---

    @Test
    void login_validCredentials_returnsAuthResponse() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com").password("Test@123!").build();
        User user = buildUser();
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        stubTokenGeneration();

        AuthService.AuthTokens result = authService.login(request);

        assertThat(result.response().getAccessToken()).isEqualTo("access-token");
    }

    // --- refreshToken ---

    @Test
    void refreshToken_success_returnsNewTokens() {
        User user = buildUser();
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

        when(jwtTokenProvider.validateToken("valid-refresh")).thenReturn(true);
        when(jwtTokenProvider.getJtiFromToken("valid-refresh")).thenReturn("jti-123");
        when(redisTemplate.hasKey("blacklisted-refresh::jti-123")).thenReturn(false);
        when(jwtTokenProvider.getUserIdFromToken("valid-refresh")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.getExpirationFromToken("valid-refresh"))
                .thenReturn(new Date(System.currentTimeMillis() + 60000));
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        stubTokenGeneration();

        AuthService.AuthTokens result = authService.refreshToken("valid-refresh");

        assertThat(result.response().getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void refreshToken_nullToken_throws() {
        assertThatThrownBy(() -> authService.refreshToken(null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void refreshToken_blankToken_throws() {
        assertThatThrownBy(() -> authService.refreshToken("   "))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void refreshToken_invalidToken_throws() {
        when(jwtTokenProvider.validateToken("bad-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("bad-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void refreshToken_revokedToken_throws() {
        when(jwtTokenProvider.validateToken("revoked")).thenReturn(true);
        when(jwtTokenProvider.getJtiFromToken("revoked")).thenReturn("jti-999");
        when(redisTemplate.hasKey("blacklisted-refresh::jti-999")).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken("revoked"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("revoked");
    }

    // --- logout ---

    @Test
    void logout_validToken_blacklists() {
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

        when(jwtTokenProvider.validateToken("valid-refresh")).thenReturn(true);
        when(jwtTokenProvider.getJtiFromToken("valid-refresh")).thenReturn("jti-1");
        when(jwtTokenProvider.getExpirationFromToken("valid-refresh"))
                .thenReturn(new Date(System.currentTimeMillis() + 60000));
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        authService.logout("valid-refresh");

        verify(redisTemplate.opsForValue()).set(anyString(), eq(true), anyLong(), any());
    }

    @Test
    void logout_nullToken_doesNothing() {
        authService.logout(null);
        verify(jwtTokenProvider, never()).getJtiFromToken(anyString());
    }

    @Test
    void logout_invalidToken_doesNothing() {
        when(jwtTokenProvider.validateToken("invalid")).thenReturn(false);

        authService.logout("invalid");

        verify(jwtTokenProvider, never()).getJtiFromToken(anyString());
    }

    // --- forgotPassword ---

    @Test
    void forgotPassword_existingUser_storesResetToken() {
        User user = buildUser();
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("test@example.com").build();
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        authService.forgotPassword(request);

        verify(valueOps).set(argThat(k -> k.startsWith("password-reset::")),
                eq(1L), eq(15L), any());
    }

    @Test
    void forgotPassword_nonExistingUser_doesNothing() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("unknown@example.com").build();
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(request);

        verify(redisTemplate, never()).opsForValue();
    }

    // --- resetPassword ---

    @Test
    void resetPassword_success_withIntegerUserId() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("reset-token").newPassword("NewPass@123!").build();
        User user = buildUser();
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("password-reset::reset-token")).thenReturn(Integer.valueOf(1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass@123!")).thenReturn("new-encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.resetPassword(request);

        verify(userRepository).save(argThat(u -> u.getPasswordHash().equals("new-encoded")));
        verify(redisTemplate).delete("password-reset::reset-token");
    }

    @Test
    void resetPassword_success_withLongUserId() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("reset-token").newPassword("NewPass@123!").build();
        User user = buildUser();
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("password-reset::reset-token")).thenReturn(Long.valueOf(1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass@123!")).thenReturn("new-encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.resetPassword(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void resetPassword_success_withStringUserId() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("reset-token").newPassword("NewPass@123!").build();
        User user = buildUser();
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("password-reset::reset-token")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass@123!")).thenReturn("new-encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.resetPassword(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void resetPassword_expiredToken_throws() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("expired-token").newPassword("NewPass@123!").build();
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("password-reset::expired-token")).thenReturn(null);

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }
}
