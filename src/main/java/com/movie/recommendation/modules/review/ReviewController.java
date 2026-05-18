package com.movie.recommendation.modules.review;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.common.dto.PagedResponse;
import com.movie.recommendation.modules.review.dto.*;
import com.movie.recommendation.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Reviews", description = "Movie review management")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/movies/{movieId}/reviews")
    @Operation(summary = "List reviews for a movie")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getMovieReviews(
            @PathVariable Long movieId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getMovieReviews(movieId, currentUserId, page, size),
                "Reviews retrieved successfully"));
    }

    @PostMapping("/movies/{movieId}/reviews")
    @Operation(summary = "Submit a review")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Review created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Already reviewed this movie")
    })
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long movieId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse review = reviewService.createReview(movieId, currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(review, "Review created successfully"));
    }

    @PutMapping("/reviews/{id}")
    @Operation(summary = "Edit own review")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not the review owner")
    })
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.updateReview(id, currentUser.getId(), request),
                "Review updated successfully"));
    }

    @DeleteMapping("/reviews/{id}")
    @Operation(summary = "Delete a review")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authorized")
    })
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        reviewService.deleteReview(id, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
    }

    @PostMapping("/reviews/{id}/like")
    @Operation(summary = "Toggle like on a review")
    public ResponseEntity<ApiResponse<Boolean>> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        boolean liked = reviewService.toggleLike(id, currentUser.getId());
        String message = liked ? "Review liked" : "Review unliked";
        return ResponseEntity.ok(ApiResponse.success(liked, message));
    }

    @PostMapping("/reviews/{id}/replies")
    @Operation(summary = "Reply to a review")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Reply created")
    })
    public ResponseEntity<ApiResponse<ReplyResponse>> createReply(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateReplyRequest request) {
        ReplyResponse reply = reviewService.createReply(id, currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(reply, "Reply created successfully"));
    }

    @GetMapping("/reviews/{id}/replies")
    @Operation(summary = "List replies for a review")
    public ResponseEntity<ApiResponse<PagedResponse<ReplyResponse>>> getReplies(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getReplies(id, page, size),
                "Replies retrieved successfully"));
    }
}
