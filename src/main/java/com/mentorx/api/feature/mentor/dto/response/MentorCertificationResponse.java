package com.mentorx.api.feature.mentor.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MentorCertificationResponse(
        UUID id,
        String certificationName,
        String issuingOrganization,
        LocalDate issueDate,
        LocalDate expiryDate,
        String credentialId,
        String credentialUrl,
        Boolean isVerified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}