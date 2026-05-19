package com.mentorx.api.feature.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record PlatformSettingRequest(
    @NotBlank(message = "Key is required")
    @Size(max = 100, message = "Key must not exceed 100 characters")
    String key,
    
    @NotBlank(message = "Value is required")
    String value,
    
    String description,
    
    UUID updatedBy
) {}
