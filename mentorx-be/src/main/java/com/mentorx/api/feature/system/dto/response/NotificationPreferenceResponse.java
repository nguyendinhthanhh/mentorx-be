package com.mentorx.api.feature.system.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NotificationPreferenceResponse(
    UUID userId,
    String userFullName,
    Boolean emailEnabled,
    Boolean pushEnabled,
    Boolean inAppEnabled,
    String emailTypeSettings,
    String pushTypeSettings,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt
) {}
