package com.mentorx.api.feature.complaint.dto.request;

import com.mentorx.api.feature.complaint.enums.ComplaintOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComplaintResolveRequest(
    @NotNull(message = "Outcome is required")
    ComplaintOutcome outcome,

    @NotBlank(message = "Resolution details are required")
    @Size(max = 2000)
    String resolutionDetails
) {}
