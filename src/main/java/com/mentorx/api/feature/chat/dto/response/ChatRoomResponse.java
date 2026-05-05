package com.mentorx.api.feature.chat.dto.response;

import com.mentorx.api.feature.chat.enums.ChatRoomType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record ChatRoomResponse(
        UUID id,
        ChatRoomType roomType,
        String roomName,
        String description,
        UUID createdByUserId,
        Boolean isActive,
        Boolean isPrivate,
        Integer maxMembers,
        Integer memberCount,
        UUID referenceId,
        String referenceType,
        LocalDateTime lastActivityAt,
        UUID lastMessageId,
        String lastMessagePreview,
        LocalDateTime lastMessageAt,
        UUID lastMessageSenderId,
        Long messageCount,
        Map<String, Object> roomSettings,
        String avatarUrl,
        Boolean isArchived,
        LocalDateTime archivedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
