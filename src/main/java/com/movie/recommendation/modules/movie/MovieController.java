package com.movie.recommendation.modules.movie;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.common.dto.PagedResponse;
import com.movie.recommendation.modules.movie.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/movies")
@Tag(name = "Movies", description = "Movie catalog management")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    @Operation(summary = "List movies (paginated, filterable)")
    public ResponseEntity<ApiResponse<PagedResponse<MovieResponse>>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) BigDecimal minRating) {
        return ResponseEntity.ok(ApiResponse.success(
                movieService.getAllMovies(page, size, sortBy, sortDir, genreId, year, minRating),
                "Movies retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie details")
    public ResponseEntity<ApiResponse<MovieDetailResponse>> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                movieService.getMovieById(id),
                "Movie retrieved successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search movies by title (optionally filter by genre)")
    public ResponseEntity<ApiResponse<PagedResponse<MovieResponse>>> searchMovies(
            @RequestParam String query,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                movieService.searchMovies(query, genreId, page, size),
                "Search results retrieved successfully"));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending movies")
    public ResponseEntity<ApiResponse<PagedResponse<MovieResponse>>> getTrendingMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                movieService.getTrendingMovies(page, size),
                "Trending movies retrieved successfully"));
    }

    @PostMapping
    @Operation(summary = "Create a new movie")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Movie created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<ApiResponse<MovieDetailResponse>> createMovie(
            @Valid @RequestBody CreateMovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(movieService.createMovie(request), "Movie created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update movie details")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Movie updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<ApiResponse<MovieDetailResponse>> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMovieRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                movieService.updateMovie(id, request), "Movie updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a movie")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Movie deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<ApiResponse<Void>> softDeleteMovie(@PathVariable Long id) {
        movieService.softDeleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Movie deactivated successfully"));
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restore a soft-deleted movie")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Movie restored"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<ApiResponse<MovieDetailResponse>> restoreMovie(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                movieService.restoreMovie(id), "Movie restored successfully"));
    }
}
