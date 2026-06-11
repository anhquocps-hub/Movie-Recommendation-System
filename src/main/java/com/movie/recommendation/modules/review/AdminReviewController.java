package com.movie.recommendation.modules.review;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.modules.review.dto.AdminReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Reviews", description = "Admin review and reply moderation")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping("/movies/{movieId}/reviews")
    @Operation(summary = "List all reviews and replies for a movie (admin moderation view)")
    public ResponseEntity<ApiResponse<List<AdminReviewResponse>>> getMovieReviews(
            @PathVariable Long movieId) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getAdminMovieReviews(movieId),
                "Admin reviews retrieved successfully"));
    }

    @PatchMapping("/reviews/{id}/hide")
    @Operation(summary = "Hide a review")
    public ResponseEntity<ApiResponse<Void>> hideReview(@PathVariable Long id) {
        reviewService.hideReview(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Review hidden successfully"));
    }

    @PatchMapping("/reviews/{id}/unhide")
    @Operation(summary = "Unhide a review")
    public ResponseEntity<ApiResponse<Void>> unhideReview(@PathVariable Long id) {
        reviewService.unhideReview(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Review unhidden successfully"));
    }

    @DeleteMapping("/reviews/{id}")
    @Operation(summary = "Soft-delete a review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.adminDeleteReview(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
    }

    @PatchMapping("/replies/{id}/hide")
    @Operation(summary = "Hide a reply")
    public ResponseEntity<ApiResponse<Void>> hideReply(@PathVariable Long id) {
        reviewService.hideReply(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Reply hidden successfully"));
    }

    @PatchMapping("/replies/{id}/unhide")
    @Operation(summary = "Unhide a reply")
    public ResponseEntity<ApiResponse<Void>> unhideReply(@PathVariable Long id) {
        reviewService.unhideReply(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Reply unhidden successfully"));
    }

    @DeleteMapping("/replies/{id}")
    @Operation(summary = "Soft-delete a reply")
    public ResponseEntity<ApiResponse<Void>> deleteReply(@PathVariable Long id) {
        reviewService.adminDeleteReply(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Reply deleted successfully"));
    }
}
