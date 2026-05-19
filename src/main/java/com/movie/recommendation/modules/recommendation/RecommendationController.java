package com.movie.recommendation.modules.recommendation;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.common.dto.PagedResponse;
import com.movie.recommendation.modules.recommendation.dto.RecommendationResponse;
import com.movie.recommendation.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
@Tag(name = "Recommendations", description = "Personalized movie recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    @Operation(summary = "Get personalized recommendations for the current user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recommendations retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<PagedResponse<RecommendationResponse>>> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                recommendationService.getUserRecommendations(currentUser.getId(), page, size),
                "Recommendations retrieved successfully"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Manually trigger recommendation generation (admin only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recommendation job triggered"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin access required")
    })
    public ResponseEntity<ApiResponse<Void>> triggerRefresh() {
        recommendationService.generateRecommendationsForAllUsers();
        return ResponseEntity.ok(ApiResponse.success(null, "Recommendation generation triggered successfully"));
    }
}
