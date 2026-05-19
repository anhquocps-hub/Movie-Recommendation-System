package com.movie.recommendation.modules.notification;

import com.movie.recommendation.modules.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.actor WHERE n.recipient.id = :recipientId ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(@Param("recipientId") Long recipientId, Pageable pageable);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.recipient.id = :recipientId")
    int markAsRead(@Param("id") Long id, @Param("recipientId") Long recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isRead = false")
    int markAllAsRead(@Param("recipientId") Long recipientId);
}
