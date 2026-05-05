package com.mentorx.api.feature.notification.dto.request;

import com.mentorx.api.feature.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record NotificationCreateRequest(
        @NotNull(message = "User ID is required") UUID userId,
        @NotNull(message = "Notification type is required") NotificationType notificationType,
        @NotBlank(message = "Title is required") @Size(max = 200) String title,
        @NotBlank(message = "Message is required") @Size(max = 1000) String message,
        UUID referenceId,
        @Size(max = 50) String referenceType,
        @Size(max = 500) String actionUrl,
        @Size(max = 500) String iconUrl,
        Integer priorityLevel,
        Map<String, Object> data,
        @Size(max = 30) String category,
        @Size(max = 100) String groupId,
        UUID senderUserId
) {}
