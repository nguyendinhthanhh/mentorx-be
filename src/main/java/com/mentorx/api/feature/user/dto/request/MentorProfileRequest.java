package com.mentorx.api.feature.user.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record MentorProfileRequest(
    @Size(max = 255, message = "Headline must not exceed 255 characters")
    String headline,

    @DecimalMin(value = "0.0", message = "Hourly rate must be positive")
    @Digits(integer = 10, fraction = 2, message = "Invalid hourly rate format")
    BigDecimal hourlyRateMxc,

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 50, message = "Years of experience cannot exceed 50")
    Short yearsOfExperience,

    @Size(max = 50, message = "Availability must not exceed 50 characters")
    String availability,

    @Min(value = 1, message = "Response time must be at least 1 hour")
    @Max(value = 168, message = "Response time cannot exceed 168 hours (1 week)")
    Short responseTimeHours,

    String cvUrl,

    String portfolioUrl
) {}