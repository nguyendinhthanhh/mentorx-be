package com.mentorx.api.feature.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRoleResponse(
    Integer roleId,
    String roleName,
    String description,
    UUID grantedBy,
    String grantedByName,
    LocalDateTime grantedAt
) {}