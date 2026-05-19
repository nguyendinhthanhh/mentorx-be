package com.mentorx.api.feature.moderation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DisputeRespondRequest(
        @NotBlank(message = "Response is required") @Size(max = 5000) String response,
        List<String> evidenceUrls
) {}
