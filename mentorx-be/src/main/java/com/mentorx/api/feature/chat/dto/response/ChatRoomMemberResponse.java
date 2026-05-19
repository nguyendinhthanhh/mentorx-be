package com.mentorx.api.feature.chat.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatRoomMemberResponse(
        UUID userId,
        String fullName,
        String displayName,
        String avatarUrl,
        String memberRole,
        Boolean isOnline,
        LocalDateTime lastSeenAt
) {}
