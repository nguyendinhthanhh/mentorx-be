package com.mentorx.api.feature.user.dto;

import com.mentorx.api.common.enums.MentorStatus;
import java.time.LocalDateTime;

public record KycStatusResponse(
    MentorStatus mentorStatus,
    String livenessResult,
    Double livenessScore,
    String faceMatchingResult,
    Double faceMatchingSimilarity,
    LocalDateTime submittedAt,
    LocalDateTime approvedAt,
    String rejectionReason,
    String identityDocumentUrl,
    String identityDocumentBackUrl,
    String portraitUrl,
    String legalName,
    String dateOfBirth
) {
}
