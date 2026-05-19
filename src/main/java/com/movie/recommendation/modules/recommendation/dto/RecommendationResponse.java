package com.movie.recommendation.modules.recommendation.dto;

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
public class RecommendationResponse {

    private Long id;
    private BigDecimal score;
    private String strategyType;
    private LocalDateTime generatedAt;
    private MovieSummary movie;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieSummary {
        private Long id;
        private String title;
        private String slug;
        private String posterUrl;
        private LocalDate releaseDate;
        private BigDecimal avgRating;
        private Integer voteCount;
        private List<String> genres;
    }
}
