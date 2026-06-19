package com.mentorx.api.feature.complaint.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComplaintRespondRequest(
    @NotBlank(message = "Response is required")
    @Size(max = 2000)
    String response
) {}
