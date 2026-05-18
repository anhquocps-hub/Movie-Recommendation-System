package com.movie.recommendation.modules.movie;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.modules.movie.dto.CreateGenreRequest;
import com.movie.recommendation.modules.movie.dto.GenreResponse;
import com.movie.recommendation.modules.movie.dto.UpdateGenreRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genres")
@Tag(name = "Genres", description = "Genre management")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    @Operation(summary = "List all genres")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Genres retrieved")
    })
    public ResponseEntity<ApiResponse<List<GenreResponse>>> getAllGenres() {
        return ResponseEntity.ok(ApiResponse.success(genreService.getAllGenres(), "Genres retrieved successfully"));
    }

    @PostMapping
    @Operation(summary = "Create a genre")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Genre created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Genre name already exists")
    })
    public ResponseEntity<ApiResponse<GenreResponse>> createGenre(@Valid @RequestBody CreateGenreRequest request) {
        GenreResponse genre = genreService.createGenre(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(genre, "Genre created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a genre")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Genre updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Genre not found")
    })
    public ResponseEntity<ApiResponse<GenreResponse>> updateGenre(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateGenreRequest request) {
        return ResponseEntity.ok(ApiResponse.success(genreService.updateGenre(id, request), "Genre updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a genre")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Genre deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Genre not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteGenre(@PathVariable Integer id) {
        genreService.deleteGenre(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Genre deleted successfully"));
    }
}
