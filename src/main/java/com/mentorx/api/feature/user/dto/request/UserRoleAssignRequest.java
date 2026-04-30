package com.mentorx.api.feature.user.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserRoleAssignRequest(
    @NotNull(message = "User ID is required")
    UUID userId,

    @NotNull(message = "Role ID is required")
    UUID roleId
) {}