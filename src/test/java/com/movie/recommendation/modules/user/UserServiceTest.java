package com.movie.recommendation.modules.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private UserService userService;

    private User buildUser() {
        return User.builder()
                .id(1L).email("test@test.com").username("testuser")
                .passwordHash("hash").role(Role.USER).isActive(true)
                .preferences("[\"action\"]")
                .build();
    }

    // --- getCurrentProfile ---

    @Test
    void getCurrentProfile_success() throws JsonProcessingException {
        User user = buildUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(objectMapper.readValue(eq("[\"action\"]"), any(TypeReference.class)))
                .thenReturn(List.of("action"));

        UserProfileResponse result = userService.getCurrentProfile(1L);

        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getPreferences()).containsExactly("action");
    }

    @Test
    void getCurrentProfile_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentProfile(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCurrentProfile_nullPreferences_returnsEmptyList() {
        User user = buildUser();
        user.setPreferences(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse result = userService.getCurrentProfile(1L);

        assertThat(result.getPreferences()).isEmpty();
    }

    @Test
    void getCurrentProfile_blankPreferences_returnsEmptyList() {
        User user = buildUser();
        user.setPreferences("   ");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse result = userService.getCurrentProfile(1L);

        assertThat(result.getPreferences()).isEmpty();
    }

    @Test
    void getCurrentProfile_malformedPreferencesJson_returnsEmptyList() throws JsonProcessingException {
        User user = buildUser();
        user.setPreferences("not-json");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(objectMapper.readValue(eq("not-json"), any(TypeReference.class)))
                .thenThrow(new JsonProcessingException("parse error") {});

        UserProfileResponse result = userService.getCurrentProfile(1L);

        assertThat(result.getPreferences()).isEmpty();
    }

    // --- updateProfile ---

    @Test
    void updateProfile_usernameChange_success() throws JsonProcessingException {
        User user = buildUser();
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newname");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newname")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(List.of("action"));

        UserProfileResponse result = userService.updateProfile(1L, request);

        assertThat(result.getUsername()).isEqualTo("newname");
    }

    @Test
    void updateProfile_sameUsername_noConflictCheck() throws JsonProcessingException {
        User user = buildUser();
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(List.of("action"));

        UserProfileResponse result = userService.updateProfile(1L, request);

        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository, never()).existsByUsername(anyString());
    }

    @Test
    void updateProfile_duplicateUsername_throws() {
        User user = buildUser();
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("taken");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile(1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updateProfile_avatarUrlOnly_success() throws JsonProcessingException {
        User user = buildUser();
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setAvatarUrl("https://example.com/avatar.jpg");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(List.of("action"));

        UserProfileResponse result = userService.updateProfile(1L, request);

        assertThat(result.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
    }

    @Test
    void updateProfile_nullUsernameAndAvatar_noChanges() throws JsonProcessingException {
        User user = buildUser();
        UpdateProfileRequest request = new UpdateProfileRequest();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(List.of("action"));

        UserProfileResponse result = userService.updateProfile(1L, request);

        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    // --- updatePreferences ---

    @Test
    void updatePreferences_success() throws JsonProcessingException {
        User user = buildUser();
        UpdatePreferencesRequest request = new UpdatePreferencesRequest();
        request.setPreferences(List.of("comedy", "drama"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(objectMapper.writeValueAsString(List.of("comedy", "drama")))
                .thenReturn("[\"comedy\",\"drama\"]");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(objectMapper.readValue(eq("[\"comedy\",\"drama\"]"), any(TypeReference.class)))
                .thenReturn(List.of("comedy", "drama"));

        UserProfileResponse result = userService.updatePreferences(1L, request);

        assertThat(result.getPreferences()).containsExactly("comedy", "drama");
    }

    @Test
    void updatePreferences_jsonException_throws() throws JsonProcessingException {
        User user = buildUser();
        UpdatePreferencesRequest request = new UpdatePreferencesRequest();
        request.setPreferences(List.of("comedy"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("err") {});

        assertThatThrownBy(() -> userService.updatePreferences(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid preferences");
    }

    // --- getUserById ---

    @Test
    void getUserById_success() throws JsonProcessingException {
        User user = buildUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(List.of("action"));

        UserProfileResponse result = userService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getAllUsers ---

    @Test
    void getAllUsers_success() throws JsonProcessingException {
        User user = buildUser();
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(List.of("action"));

        PagedResponse<UserProfileResponse> result = userService.getAllUsers(0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllUsers_enforcesMaxPageSize() throws JsonProcessingException {
        when(userRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        userService.getAllUsers(0, 200);

        verify(userRepository).findAll(argThat((Pageable p) -> p.getPageSize() == 50));
    }

    // --- changeRole ---

    @Test
    void changeRole_success() throws JsonProcessingException {
        User user = buildUser();
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(List.of("action"));

        UserProfileResponse result = userService.changeRole(1L, request);

        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void changeRole_invalidRole_throws() {
        User user = buildUser();
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole("SUPERADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changeRole(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid role");
    }

    @Test
    void changeRole_lowercaseRole_success() throws JsonProcessingException {
        User user = buildUser();
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole("admin");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(List.of("action"));

        UserProfileResponse result = userService.changeRole(1L, request);

        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    // --- deactivateUser ---

    @Test
    void deactivateUser_success() {
        User user = buildUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.deactivateUser(1L);

        assertThat(user.getIsActive()).isFalse();
    }

    @Test
    void deactivateUser_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
