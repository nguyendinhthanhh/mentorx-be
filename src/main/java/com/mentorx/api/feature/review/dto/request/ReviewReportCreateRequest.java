package com.mentorx.api.feature.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ReviewReportCreateRequest(
        @NotNull(message = "Reporter ID is required") UUID reporterId,
        
        @NotBlank(message = "Report reason is required")
        @Size(max = 50, message = "Report reason must not exceed 50 characters")
        String reportReason,
        
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {}
