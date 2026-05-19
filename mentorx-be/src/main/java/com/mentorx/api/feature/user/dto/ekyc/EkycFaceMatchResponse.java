package com.mentorx.api.feature.user.dto.ekyc;

public record EkycFaceMatchResponse(
    int code,
    boolean isMatch,
    double similarity,
    String message
) {
}
