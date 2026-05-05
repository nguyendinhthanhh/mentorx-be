package com.mentorx.api.feature.system.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record NotificationPreferenceRequest(
    @NotNull(message = "User ID is required")
    UUID userId,
    
    Boolean emailEnabled,
    
    Boolean pushEnabled,
    
    Boolean inAppEnabled,
    
    String emailTypeSettings,
    
    String pushTypeSettings
) {}
