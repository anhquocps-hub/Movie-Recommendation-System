package com.movie.recommendation.modules.notification;

import com.movie.recommendation.modules.notification.entity.Notification;
import com.movie.recommendation.modules.notification.event.NotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener listener;

    @Test
    void handleReviewLiked_callsCreateAndSend() {
        NotificationEvent.ReviewLikedEvent event = new NotificationEvent.ReviewLikedEvent(
                10L, 1L, 2L, "actor", "Test Movie");

        listener.handleReviewLiked(event);

        verify(notificationService).createAndSend(
                eq(1L), eq(2L),
                eq(Notification.NotificationType.REVIEW_LIKE),
                eq(10L),
                contains("actor liked your review"));
    }

    @Test
    void handleReviewReplied_callsCreateAndSend() {
        NotificationEvent.ReviewRepliedEvent event = new NotificationEvent.ReviewRepliedEvent(
                10L, 1L, 2L, "actor", "Test Movie", "Great review!");

        listener.handleReviewReplied(event);

        verify(notificationService).createAndSend(
                eq(1L), eq(2L),
                eq(Notification.NotificationType.REVIEW_REPLY),
                eq(10L),
                contains("actor replied to your review"));
    }

    @Test
    void handleNewRecommendation_callsCreateAndSend() {
        NotificationEvent.NewRecommendationEvent event = new NotificationEvent.NewRecommendationEvent(1L, null);

        listener.handleNewRecommendation(event);

        verify(notificationService).createAndSend(
                eq(1L), isNull(),
                eq(Notification.NotificationType.NEW_RECOMMENDATION),
                isNull(),
                contains("recommendations have been updated"));
    }
}
