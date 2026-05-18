package com.movie.recommendation.modules.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReplyRequest {

    @NotBlank(message = "Reply content is required")
    @Size(max = 5000, message = "Reply content must be at most 5000 characters")
    private String content;
}
