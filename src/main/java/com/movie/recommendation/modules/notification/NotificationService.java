package com.movie.recommendation.modules.notification;

import com.movie.recommendation.common.constants.AppConstants;
import com.movie.recommendation.common.dto.PagedResponse;
import com.movie.recommendation.exception.ResourceNotFoundException;
import com.movie.recommendation.modules.notification.dto.NotificationResponse;
import com.movie.recommendation.modules.notification.entity.Notification;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public PagedResponse<NotificationResponse> getUserNotifications(Long userId, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
        return PagedResponse.from(notifications);
    }

    @Transactional
    @CacheEvict(value = AppConstants.NOTIFICATION_CACHE, key = "#userId")
    public void markAsRead(Long notificationId, Long userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId);
        if (updated == 0) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }
    }

    @Transactional
    @CacheEvict(value = AppConstants.NOTIFICATION_CACHE, key = "#userId")
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Cacheable(value = AppConstants.NOTIFICATION_CACHE, key = "#userId")
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    @CacheEvict(value = AppConstants.NOTIFICATION_CACHE, key = "#recipientId")
    public void createAndSend(Long recipientId, Long actorId,
                              Notification.NotificationType type,
                              Long referenceId, String message) {
        User recipient = userRepository.getReferenceById(recipientId);
        User actor = actorId != null ? userRepository.getReferenceById(actorId) : null;

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .referenceId(referenceId)
                .message(message)
                .build();

        notification = notificationRepository.save(notification);

        NotificationResponse payload = toResponse(notification);
        messagingTemplate.convertAndSendToUser(
                String.valueOf(recipientId),
                "/queue/notifications",
                payload
        );
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .actorId(n.getActor() != null ? n.getActor().getId() : null)
                .actorName(n.getActor() != null ? n.getActor().getUsername() : "System")
                .referenceId(n.getReferenceId())
                .message(n.getMessage())
                .read(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
