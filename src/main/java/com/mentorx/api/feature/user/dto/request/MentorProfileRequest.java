package com.mentorx.api.feature.user.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Min(value = 1, message = "Response time must be at least 1 hour")
    @Max(value = 168, message = "Response time cannot exceed 168 hours (1 week)")
    Short responseTimeHours,

    String cvUrl,

    String portfolioUrl,

    String videoIntroUrl,

    @Size(max = 150, message = "Location must not exceed 150 characters")
    String location,

    List<String> languages,

    @Size(max = 150, message = "Legal name must not exceed 150 characters")
    String legalName,

    LocalDate dateOfBirth,

    @Size(max = 100, message = "Country of residence must not exceed 100 characters")
    String countryOfResidence,

    @Size(max = 50, message = "Identity document type must not exceed 50 characters")
    String identityDocumentType,

    String identityDocumentUrl,

    String portraitUrl,

    @Size(max = 30, message = "Phone number must not exceed 30 characters")
    String phoneNumber,

    Boolean phoneVerified,

    @Size(max = 150, message = "Current title must not exceed 150 characters")
    String currentTitle,

    @Size(max = 150, message = "Current company must not exceed 150 characters")
    String currentCompany,

    @Size(max = 120, message = "Primary domain must not exceed 120 characters")
    String primaryDomain,

    String linkedinUrl,

    String githubUrl,

    String portfolioEvidenceUrl,

    String certificateUrl,

    @Size(max = 150, message = "Bank account name must not exceed 150 characters")
    String bankAccountName,

    @Size(max = 150, message = "Bank name must not exceed 150 characters")
    String bankName,

    @Size(max = 80, message = "Bank account number must not exceed 80 characters")
    String bankAccountNumber,

    @Size(max = 150, message = "Bank branch must not exceed 150 characters")
    String bankBranch,

    @Size(max = 80, message = "Tax ID must not exceed 80 characters")
    String taxId,

    Boolean mentorAgreementAccepted,

    Boolean disputePolicyAccepted
) {}
