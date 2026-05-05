package com.mentorx.api.feature.notification.repository;

import com.mentorx.api.feature.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserIdAndIsDismissedFalse(UUID userId, Pageable pageable);
    Page<Notification> findByUserIdAndIsReadFalseAndIsDismissedFalse(UUID userId, Pageable pageable);
    long countByUserIdAndIsReadFalseAndIsDismissedFalse(UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadByUserId(UUID userId);
}
