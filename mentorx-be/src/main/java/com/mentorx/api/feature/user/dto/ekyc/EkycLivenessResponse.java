package com.mentorx.api.feature.user.dto.ekyc;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EkycLivenessResponse(
    int code,
    @JsonProperty("is_live") boolean isLive,
    @JsonProperty("liveness_score") double livenessScore,
    String message
) {
}
