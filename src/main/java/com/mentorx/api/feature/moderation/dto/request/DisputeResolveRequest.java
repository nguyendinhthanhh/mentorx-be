package com.mentorx.api.feature.moderation.dto.request;

import com.mentorx.api.feature.moderation.enums.DisputeOutcome;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DisputeResolveRequest(
        @NotNull(message = "Outcome is required") DisputeOutcome outcome,
        @Size(max = 2000) String resolutionDetails,
        BigDecimal refundAmountMxc
) {}
