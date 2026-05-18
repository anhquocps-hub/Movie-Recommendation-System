package com.movie.recommendation.modules.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.recommendation.common.constants.AppConstants;
import com.movie.recommendation.common.dto.PagedResponse;
import com.movie.recommendation.exception.BadRequestException;
import com.movie.recommendation.exception.DuplicateResourceException;
import com.movie.recommendation.exception.ResourceNotFoundException;
import com.movie.recommendation.modules.user.dto.UpdatePreferencesRequest;
import com.movie.recommendation.modules.user.dto.UpdateProfileRequest;
import com.movie.recommendation.modules.user.dto.UpdateRoleRequest;
import com.movie.recommendation.modules.user.dto.UserProfileResponse;
import com.movie.recommendation.modules.user.entity.Role;
import com.movie.recommendation.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserProfileResponse getCurrentProfile(Long userId) {
        User user = findUserById(userId);
        return toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        if (request.getUsername() != null) {
            if (!request.getUsername().equals(user.getUsername())
                    && userRepository.existsByUsername(request.getUsername())) {
                throw new DuplicateResourceException("User", "username", request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        return toProfileResponse(userRepository.save(user));
    }

    @Transactional
    public UserProfileResponse updatePreferences(Long userId, UpdatePreferencesRequest request) {
        User user = findUserById(userId);
        user.setPreferences(toJson(request.getPreferences()));
        return toProfileResponse(userRepository.save(user));
    }

    public UserProfileResponse getUserById(Long userId) {
        User user = findUserById(userId);
        return toProfileResponse(user);
    }

    public PagedResponse<UserProfileResponse> getAllUsers(int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserProfileResponse> users = userRepository.findAll(pageable)
                .map(this::toProfileResponse);
        return PagedResponse.from(users);
    }

    @Transactional
    public UserProfileResponse changeRole(Long userId, UpdateRoleRequest request) {
        User user = findUserById(userId);
        try {
            Role role = Role.valueOf(request.getRole().toUpperCase());
            user.setRole(role);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + request.getRole());
        }
        return toProfileResponse(userRepository.save(user));
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = findUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .preferences(parsePreferences(user.getPreferences()))
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private List<String> parsePreferences(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private String toJson(List<String> preferences) {
        try {
            return objectMapper.writeValueAsString(preferences);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid preferences format");
        }
    }
}
