package com.mentorx.api.feature.chat.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserBlockResponse(
        UUID id,
        UUID blockerUserId,
        UUID blockedUserId,
        LocalDateTime blockedAt,
        Boolean isActive,
        String blockReason,
        String blockType,
        Boolean isTemporary,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
