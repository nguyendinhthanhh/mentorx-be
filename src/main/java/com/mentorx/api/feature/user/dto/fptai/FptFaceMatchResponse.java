package com.mentorx.api.feature.user.dto.fptai;

public record FptFaceMatchResponse(
    int code,
    boolean isMatch,
    double similarity,
    String message
) {
}
