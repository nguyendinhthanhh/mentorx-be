package com.mentorx.api.feature.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserBlockRequest(
        @NotNull(message = "Blocker ID is required") UUID blockerUserId,
        @NotNull(message = "Blocked User ID is required") UUID blockedUserId,
        @Size(max = 500) String blockReason,
        @Size(max = 20) String blockType,
        Boolean isTemporary,
        Integer durationHours
) {}
