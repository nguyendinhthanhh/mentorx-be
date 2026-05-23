package com.mentorx.api.feature.user.dto;

import com.mentorx.api.common.enums.IdentityDocumentType;
import com.mentorx.api.common.enums.VerificationStatus;
import java.time.LocalDateTime;

public record KycStatusResponse(
    VerificationStatus identityStatus,
    Boolean identityRequired,
    IdentityDocumentType documentType,
    String livenessResult,
    Double livenessScore,
    String faceMatchingResult,
    Double faceMatchingSimilarity,
    LocalDateTime submittedAt,
    LocalDateTime approvedAt,
    String rejectionReason,
    String legalName,
    String dateOfBirth,
    String countryOfResidence,
    String documentNumberMasked,
    String verificationProvider
) {
}
