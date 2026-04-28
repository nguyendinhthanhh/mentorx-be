package com.mentorx.api.feature.user.dto.response;

import java.time.LocalDateTime;

public record RoleResponse(
    Integer id,
    String roleName,
    String description,
    LocalDateTime createdAt,
    Long userCount
) {}