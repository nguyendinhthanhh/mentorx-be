package com.mentorx.api.feature.system.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CategoryResponse(
    Integer id,
    Integer categoryId, // Alias for id to match frontend
    String name,      // Alias for labelEn/Vi to match frontend
    String slug,
    String labelVi,
    String labelEn,
    String labelZh,
    String labelJa,
    String iconUrl,
    Integer parentId,
    Integer parentCategoryId, // Alias for parentId to match frontend
    String parentName,
    Boolean isActive,
    Short displayOrder,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt
) {}
