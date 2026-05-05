package com.mentorx.api.feature.moderation.dto.request;

import com.mentorx.api.feature.moderation.enums.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ReportCreateRequest(
        @NotNull(message = "Reporter ID is required") UUID reporterId,
        @NotNull(message = "Target type is required") ReportTargetType targetType,
        @NotNull(message = "Target ID is required") UUID targetId,
        UUID reportedUserId,
        @NotBlank(message = "Report category is required") @Size(max = 50) String reportCategory,
        @NotBlank(message = "Reason is required") @Size(max = 2000) String reason,
        List<String> evidenceUrls,
        String reportContext
) {}
