package com.mentorx.api.feature.chat.dto.request;

import com.mentorx.api.feature.chat.enums.ChatRoomType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ChatRoomCreateRequest(
        @NotNull(message = "Room type is required") ChatRoomType roomType,
        @Size(max = 200) String roomName,
        @Size(max = 500) String description,
        @NotNull(message = "Creator ID is required") UUID createdByUserId,
        Boolean isPrivate,
        Integer maxMembers,
        UUID referenceId,
        @Size(max = 50) String referenceType,
        List<UUID> memberIds
) {}
