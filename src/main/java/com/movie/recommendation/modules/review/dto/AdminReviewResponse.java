package com.movie.recommendation.modules.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewResponse {

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
    private boolean hidden;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AdminReplyResponse> replies;
}
