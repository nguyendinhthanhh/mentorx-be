package com.mentorx.api.feature.notification.dto.response;

import com.mentorx.api.feature.notification.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        NotificationType notificationType,
        String title,
        String message,
        UUID referenceId,
        String referenceType,
        String actionUrl,
        String iconUrl,
        Integer priorityLevel,
        Boolean isRead,
        LocalDateTime readAt,
        Map<String, Object> data,
        String category,
        String groupId,
        Boolean isDismissible,
        Boolean requiresAction,
        Boolean actionTaken,
        LocalDateTime actionTakenAt,
        LocalDateTime expiresAt,
        Boolean isExpired,
        UUID senderUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
