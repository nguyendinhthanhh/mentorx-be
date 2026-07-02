package com.mentorx.api.feature.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewResponseRequest(
        @NotBlank(message = "Response text is required")
        @Size(max = 1000, message = "Response text must not exceed 1000 characters")
        String responseText
) {}
