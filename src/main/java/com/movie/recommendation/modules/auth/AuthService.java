package com.movie.recommendation.modules.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.recommendation.common.constants.AppConstants;
import com.movie.recommendation.exception.BadRequestException;
import com.movie.recommendation.exception.DuplicateResourceException;
import com.movie.recommendation.exception.ResourceNotFoundException;
import com.movie.recommendation.exception.UnauthorizedException;
import com.movie.recommendation.modules.auth.dto.*;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.Role;
import com.movie.recommendation.modules.user.entity.User;
import com.movie.recommendation.security.CustomUserDetails;
import com.movie.recommendation.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public record AuthTokens(AuthResponse response, String refreshToken) {}

    @Transactional
    public AuthTokens register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        String preferencesJson = "[]";
        if (request.getPreferences() != null && !request.getPreferences().isEmpty()) {
            try {
                preferencesJson = objectMapper.writeValueAsString(request.getPreferences());
            } catch (JsonProcessingException e) {
                preferencesJson = "[]";
            }
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .preferences(preferencesJson)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        return generateTokens(user);
    }

    public AuthTokens login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        return generateTokens(user);
    }

    public AuthTokens refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing");
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String jti = jwtTokenProvider.getJtiFromToken(refreshToken);
        String blacklistKey = AppConstants.BLACKLISTED_REFRESH_PREFIX + jti;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        blacklistRefreshToken(refreshToken);

        return generateTokens(user);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            blacklistRefreshToken(refreshToken);
        }
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            String key = AppConstants.PASSWORD_RESET_PREFIX + token;
            redisTemplate.opsForValue().set(key, user.getId(),
                    AppConstants.PASSWORD_RESET_TTL_MINUTES, TimeUnit.MINUTES);
            log.info("Password reset token for {}: {}", user.getEmail(), token);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String key = AppConstants.PASSWORD_RESET_PREFIX + request.getToken();
        Object userIdObj = redisTemplate.opsForValue().get(key);

        if (userIdObj == null) {
            throw new BadRequestException("Invalid or expired password reset token");
        }

        Long userId;
        if (userIdObj instanceof Integer) {
            userId = ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            userId = (Long) userIdObj;
        } else {
            userId = Long.parseLong(userIdObj.toString());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete(key);
    }

    private AuthTokens generateTokens(User user) {
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirySeconds())
                .user(AuthResponse.UserSummary.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .role(user.getRole().name())
                        .build())
                .build();

        return new AuthTokens(response, refreshToken);
    }

    private void blacklistRefreshToken(String refreshToken) {
        String jti = jwtTokenProvider.getJtiFromToken(refreshToken);
        Date expiration = jwtTokenProvider.getExpirationFromToken(refreshToken);
        long remainingMs = expiration.getTime() - System.currentTimeMillis();
        if (remainingMs > 0) {
            String blacklistKey = AppConstants.BLACKLISTED_REFRESH_PREFIX + jti;
            redisTemplate.opsForValue().set(blacklistKey, true, remainingMs, TimeUnit.MILLISECONDS);
        }
    }
}
