package com.movie.recommendation.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String type;
    private Long actorId;
    private String actorName;
    private Long referenceId;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
