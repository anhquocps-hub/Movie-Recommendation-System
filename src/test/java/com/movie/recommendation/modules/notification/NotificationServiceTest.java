package com.movie.recommendation.modules.notification;

import com.movie.recommendation.common.dto.PagedResponse;
import com.movie.recommendation.exception.ResourceNotFoundException;
import com.movie.recommendation.modules.notification.dto.NotificationResponse;
import com.movie.recommendation.modules.notification.entity.Notification;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.Role;
import com.movie.recommendation.modules.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private User buildUser(Long id, String username) {
        return User.builder().id(id).email(username + "@test.com").username(username)
                .passwordHash("hash").role(Role.USER).isActive(true).build();
    }

    private Notification buildNotification(Long id, User recipient, User actor, Notification.NotificationType type) {
        return Notification.builder()
                .id(id)
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .referenceId(1L)
                .message("Test notification")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getUserNotifications_returnsPaginatedResults() {
        User recipient = buildUser(1L, "recipient");
        User actor = buildUser(2L, "actor");
        Notification n = buildNotification(1L, recipient, actor, Notification.NotificationType.REVIEW_LIKE);

        Pageable pageable = PageRequest.of(0, 20);
        Page<Notification> page = new PageImpl<>(List.of(n), pageable, 1);
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);

        PagedResponse<NotificationResponse> result = notificationService.getUserNotifications(1L, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo("REVIEW_LIKE");
        assertThat(result.getContent().get(0).getActorName()).isEqualTo("actor");
    }

    @Test
    void markAsRead_success() {
        when(notificationRepository.markAsRead(1L, 1L)).thenReturn(1);

        notificationService.markAsRead(1L, 1L);

        verify(notificationRepository).markAsRead(1L, 1L);
    }

    @Test
    void markAsRead_notFound_throws() {
        when(notificationRepository.markAsRead(99L, 1L)).thenReturn(0);

        assertThatThrownBy(() -> notificationService.markAsRead(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markAllAsRead_success() {
        notificationService.markAllAsRead(1L);

        verify(notificationRepository).markAllAsRead(1L);
    }

    @Test
    void getUnreadCount_returnsCount() {
        when(notificationRepository.countByRecipientIdAndIsReadFalse(1L)).thenReturn(5L);

        long result = notificationService.getUnreadCount(1L);

        assertThat(result).isEqualTo(5L);
    }

    @Test
    void createAndSend_savesAndPushesWebSocket() {
        User recipient = buildUser(1L, "recipient");
        User actor = buildUser(2L, "actor");

        when(userRepository.getReferenceById(1L)).thenReturn(recipient);
        when(userRepository.getReferenceById(2L)).thenReturn(actor);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            n.setCreatedAt(LocalDateTime.now());
            return n;
        });

        notificationService.createAndSend(1L, 2L,
                Notification.NotificationType.REVIEW_LIKE, 10L, "Actor liked your review");

        verify(notificationRepository).save(any(Notification.class));

        ArgumentCaptor<NotificationResponse> captor = ArgumentCaptor.forClass(NotificationResponse.class);
        verify(messagingTemplate).convertAndSendToUser(eq("1"), eq("/queue/notifications"), captor.capture());

        NotificationResponse payload = captor.getValue();
        assertThat(payload.getType()).isEqualTo("REVIEW_LIKE");
        assertThat(payload.getMessage()).isEqualTo("Actor liked your review");
    }
}
