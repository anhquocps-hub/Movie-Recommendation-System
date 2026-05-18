package com.movie.recommendation.modules.watchlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistResponse {

    private Long id;
    private Long movieId;
    private String movieTitle;
    private String movieSlug;
    private String posterUrl;
    private BigDecimal avgRating;
    private LocalDateTime addedAt;
}
