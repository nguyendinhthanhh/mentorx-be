package com.mentorx.api.feature.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CategoryRequest(
    @NotBlank(message = "Slug is required")
    @Size(max = 100, message = "Slug must not exceed 100 characters")
    String slug,
    
    @NotBlank(message = "Vietnamese label is required")
    @Size(max = 100, message = "Vietnamese label must not exceed 100 characters")
    String labelVi,
    
    @NotBlank(message = "English label is required")
    @Size(max = 100, message = "English label must not exceed 100 characters")
    String labelEn,
    
    @Size(max = 100, message = "Chinese label must not exceed 100 characters")
    String labelZh,
    
    @Size(max = 100, message = "Japanese label must not exceed 100 characters")
    String labelJa,
    
    String iconUrl,
    
    Integer parentId,
    
    Boolean isActive,
    
    Short displayOrder
) {}
