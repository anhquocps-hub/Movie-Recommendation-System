package com.movie.recommendation.modules.notification.event;

public sealed interface NotificationEvent {

    record ReviewLikedEvent(
            Long reviewId,
            Long reviewOwnerId,
            Long actorId,
            String actorName,
            String movieTitle
    ) implements NotificationEvent {}

    record ReviewRepliedEvent(
            Long reviewId,
            Long reviewOwnerId,
            Long actorId,
            String actorName,
            String movieTitle,
            String replyPreview
    ) implements NotificationEvent {}

    record NewRecommendationEvent(
            Long userId,
            Long actorId
    ) implements NotificationEvent {}
}
