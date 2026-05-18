package com.movie.recommendation.modules.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDetailResponse {

    private Long id;
    private String title;
    private String slug;
    private String overview;
    private String posterUrl;
    private String backdropUrl;
    private LocalDate releaseDate;
    private Integer runtimeMinutes;
    private BigDecimal avgRating;
    private Integer voteCount;
    private Boolean isActive;
    private List<GenreResponse> genres;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
