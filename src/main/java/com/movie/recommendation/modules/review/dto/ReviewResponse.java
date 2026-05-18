package com.movie.recommendation.modules.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long userId;
    private String username;
    private Long movieId;
    private String movieTitle;
    private Short rating;
    private String content;
    private Boolean isSpoiler;
    private long likeCount;
    private long replyCount;
    private boolean likedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
