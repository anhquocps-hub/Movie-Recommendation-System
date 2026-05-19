package com.movie.recommendation.modules.notification;

import com.movie.recommendation.modules.notification.entity.Notification;
import com.movie.recommendation.modules.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleReviewLiked(NotificationEvent.ReviewLikedEvent event) {
        log.debug("Handling ReviewLikedEvent: review={}, actor={}", event.reviewId(), event.actorId());
        String message = event.actorName() + " liked your review on \"" + event.movieTitle() + "\"";
        notificationService.createAndSend(
                event.reviewOwnerId(),
                event.actorId(),
                Notification.NotificationType.REVIEW_LIKE,
                event.reviewId(),
                message
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleReviewReplied(NotificationEvent.ReviewRepliedEvent event) {
        log.debug("Handling ReviewRepliedEvent: review={}, actor={}", event.reviewId(), event.actorId());
        String message = event.actorName() + " replied to your review on \"" + event.movieTitle() + "\": "
                + event.replyPreview();
        notificationService.createAndSend(
                event.reviewOwnerId(),
                event.actorId(),
                Notification.NotificationType.REVIEW_REPLY,
                event.reviewId(),
                message
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleNewRecommendation(NotificationEvent.NewRecommendationEvent event) {
        log.debug("Handling NewRecommendationEvent for user={}", event.userId());
        String message = "Your personalized recommendations have been updated!";
        notificationService.createAndSend(
                event.userId(),
                event.actorId(),
                Notification.NotificationType.NEW_RECOMMENDATION,
                null,
                message
        );
    }
}
