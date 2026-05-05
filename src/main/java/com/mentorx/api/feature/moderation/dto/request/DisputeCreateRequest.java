package com.mentorx.api.feature.moderation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DisputeCreateRequest(
        @NotNull(message = "Initiator ID is required") UUID initiatorId,
        @NotNull(message = "Respondent ID is required") UUID respondentId,
        UUID contractId,
        UUID jobId,
        @NotBlank(message = "Title is required") @Size(max = 200) String title,
        @NotBlank(message = "Description is required") @Size(max = 5000) String description,
        @NotBlank(message = "Dispute category is required") @Size(max = 50) String disputeCategory,
        BigDecimal disputedAmountMxc,
        BigDecimal refundRequestedMxc,
        List<String> evidenceUrls
) {}
