package com.movie.recommendation.modules.movie.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMovieRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must be at most 500 characters")
    private String title;

    private String overview;

    @Size(max = 500)
    private String posterUrl;

    @Size(max = 500)
    private String backdropUrl;

    private LocalDate releaseDate;

    @Min(value = 1, message = "Runtime must be at least 1 minute")
    private Integer runtimeMinutes;

    @NotEmpty(message = "At least one genre is required")
    private Set<Integer> genreIds;
}
