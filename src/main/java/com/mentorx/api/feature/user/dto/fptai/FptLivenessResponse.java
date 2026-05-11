package com.mentorx.api.feature.user.dto.fptai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FptLivenessResponse(
    int code,
    @JsonProperty("is_live") boolean isLive,
    @JsonProperty("liveness_score") double livenessScore,
    String message
) {
}
