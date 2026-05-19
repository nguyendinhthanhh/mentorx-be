package com.mentorx.api.feature.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoleResponse(
    UUID id,
    String roleName,
    String description,
    LocalDateTime createdAt,
    Long userCount
) {}