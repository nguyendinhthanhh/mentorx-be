package com.mentorx.api.feature.system.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PlatformSettingResponse(
    String key,
    String value,
    String description,
    UUID updatedBy,
    String updatedByName,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt
) {}
