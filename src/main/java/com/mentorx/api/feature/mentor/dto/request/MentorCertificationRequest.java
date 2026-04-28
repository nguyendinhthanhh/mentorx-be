package com.mentorx.api.feature.mentor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MentorCertificationRequest(
        @NotBlank(message = "Certification name is required")
        @Size(max = 200, message = "Certification name must not exceed 200 characters")
        String certificationName,

        @NotBlank(message = "Issuing organization is required")
        @Size(max = 200, message = "Issuing organization must not exceed 200 characters")
        String issuingOrganization,

        LocalDate issueDate,

        LocalDate expiryDate,

        @Size(max = 100, message = "Credential ID must not exceed 100 characters")
        String credentialId,

        String credentialUrl
) {
}