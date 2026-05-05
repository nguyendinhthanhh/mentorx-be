package com.mentorx.api.feature.chat.dto.response;

import com.mentorx.api.feature.chat.enums.MessageType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID chatRoomId,
        UUID senderId,
        String senderName,
        String senderAvatarUrl,
        MessageType messageType,
        String content,
        LocalDateTime sentAt,
        UUID replyToMessageId,
        Boolean isEdited,
        LocalDateTime editedAt,
        Boolean isDeleted,
        String attachmentUrl,
        String attachmentFilename,
        String attachmentMimeType,
        Long attachmentSize,
        Map<String, Object> metadata,
        Integer readCount,
        Boolean isSystemMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
