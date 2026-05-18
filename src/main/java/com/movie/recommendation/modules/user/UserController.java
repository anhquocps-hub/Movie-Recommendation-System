package com.movie.recommendation.modules.user;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.common.dto.PagedResponse;
import com.movie.recommendation.modules.user.dto.UpdatePreferencesRequest;
import com.movie.recommendation.modules.user.dto.UpdateProfileRequest;
import com.movie.recommendation.modules.user.dto.UpdateRoleRequest;
import com.movie.recommendation.modules.user.dto.UserProfileResponse;
import com.movie.recommendation.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profile and admin management")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getCurrentProfile(currentUser.getId()),
                "Profile retrieved successfully"));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateProfile(currentUser.getId(), request),
                "Profile updated successfully"));
    }

    @PutMapping("/me/preferences")
    @Operation(summary = "Update current user preferences")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updatePreferences(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updatePreferences(currentUser.getId(), request),
                "Preferences updated successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all users (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserProfileResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getAllUsers(page, size),
                "Users retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getUserById(id),
                "User retrieved successfully"));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Change user role (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.changeRole(id, request),
                "Role updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate user (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated successfully"));
    }
}
