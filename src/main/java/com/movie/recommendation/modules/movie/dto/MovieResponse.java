package com.movie.recommendation.modules.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {

    private Long id;
    private String title;
    private String slug;
    private String posterUrl;
    private LocalDate releaseDate;
    private BigDecimal avgRating;
    private Integer voteCount;
    private List<String> genres;
}
