package com.mentorx.api.feature.job.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ContractCancellationDecisionRequest(
        @NotNull(message = "Mentor ID is required")
        UUID mentorId,
        @NotBlank(message = "Decision note is required")
        @Size(max = 500, message = "Decision note must not exceed 500 characters")
        String note
) {}
