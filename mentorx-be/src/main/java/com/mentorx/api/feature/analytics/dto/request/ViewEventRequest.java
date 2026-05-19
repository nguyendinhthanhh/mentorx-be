package com.mentorx.api.feature.analytics.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ViewEventRequest(
        @NotBlank(message = "Target type is required") String targetType,
        @NotNull(message = "Target ID is required") UUID targetId,
        String ipAddress
) {}
