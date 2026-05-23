package com.mentorx.api.feature.user.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

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

    String cvUrl,

    String portfolioUrl,

    String videoIntroUrl,

    @Size(max = 150, message = "Location must not exceed 150 characters")
    String location,

    List<String> languages,

    @Size(max = 150, message = "Current title must not exceed 150 characters")
    String currentTitle,

    @Size(max = 150, message = "Current company must not exceed 150 characters")
    String currentCompany,

    @Size(max = 120, message = "Primary domain must not exceed 120 characters")
    String primaryDomain,

    List<@Size(min = 1, max = 60, message = "Each skill must be between 1 and 60 characters") String> skills,

    @Size(min = 50, max = 500, message = "Professional bio must be 50 to 500 characters")
    String professionalBio,

    @Size(min = 30, max = 500, message = "Help description must be 30 to 500 characters")
    String helpDescription,

    String linkedinUrl,

    String githubUrl,

    String portfolioEvidenceUrl,

    String certificateUrl,

    Boolean mentorAgreementAccepted,

    Boolean disputePolicyAccepted
) {}
