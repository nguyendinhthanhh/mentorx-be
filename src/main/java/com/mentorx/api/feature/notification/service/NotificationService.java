package com.mentorx.api.feature.notification.service;

import com.mentorx.api.feature.notification.dto.request.NotificationCreateRequest;
import com.mentorx.api.feature.notification.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    NotificationResponse sendNotification(NotificationCreateRequest request);
    Page<NotificationResponse> getUserNotifications(UUID userId, boolean unreadOnly, Pageable pageable);
    long getUnreadCount(UUID userId);
    NotificationResponse markAsRead(UUID notificationId);
    void markAllAsRead(UUID userId);
    NotificationResponse dismissNotification(UUID notificationId);
}
