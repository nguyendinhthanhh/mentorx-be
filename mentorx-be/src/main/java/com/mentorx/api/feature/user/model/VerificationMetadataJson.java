package com.mentorx.api.feature.user.model;

import java.time.LocalDateTime;

public record VerificationMetadataJson(
    String ocrFrontRaw,
    String ocrBackRaw,
    Double livenessScore,
    String livenessResult,
    Double faceMatchingSimilarity,
    String faceMatchingResult,
    LocalDateTime verifiedAt
) {
}
