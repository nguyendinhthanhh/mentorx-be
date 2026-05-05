package com.mentorx.api.feature.chat.dto.request;

import com.mentorx.api.feature.chat.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record MessageSendRequest(
        @NotNull(message = "Chat room ID is required") UUID chatRoomId,
        @NotNull(message = "Sender ID is required") UUID senderId,
        @NotNull(message = "Message type is required") MessageType messageType,
        String content,
        UUID replyToMessageId,
        @Size(max = 500) String attachmentUrl,
        @Size(max = 255) String attachmentFilename,
        @Size(max = 100) String attachmentMimeType,
        Long attachmentSize,
        Map<String, Object> metadata
) {}
