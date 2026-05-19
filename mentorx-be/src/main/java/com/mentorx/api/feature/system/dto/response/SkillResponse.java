package com.mentorx.api.feature.system.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SkillResponse(
    Integer id,
    String slug,
    String labelVi,
    String labelEn,
    String labelZh,
    String labelJa,
    Boolean isActive,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt
) {}
