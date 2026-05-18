package com.movie.recommendation.modules.watchlist;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.common.dto.PagedResponse;
import com.movie.recommendation.modules.watchlist.dto.WatchlistResponse;
import com.movie.recommendation.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/watchlist")
@Tag(name = "Watchlist", description = "User watchlist management")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @GetMapping
    @Operation(summary = "Get user's watchlist")
    public ResponseEntity<ApiResponse<PagedResponse<WatchlistResponse>>> getWatchlist(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                watchlistService.getUserWatchlist(currentUser.getId(), page, size),
                "Watchlist retrieved successfully"));
    }

    @PostMapping("/{movieId}")
    @Operation(summary = "Add movie to watchlist")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Added to watchlist"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Movie already in watchlist")
    })
    public ResponseEntity<ApiResponse<WatchlistResponse>> addToWatchlist(
            @PathVariable Long movieId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        WatchlistResponse item = watchlistService.addToWatchlist(currentUser.getId(), movieId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(item, "Added to watchlist"));
    }

    @DeleteMapping("/{movieId}")
    @Operation(summary = "Remove movie from watchlist")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Removed from watchlist"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Movie not in watchlist")
    })
    public ResponseEntity<ApiResponse<Void>> removeFromWatchlist(
            @PathVariable Long movieId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        watchlistService.removeFromWatchlist(currentUser.getId(), movieId);
        return ResponseEntity.ok(ApiResponse.success(null, "Removed from watchlist"));
    }
}
