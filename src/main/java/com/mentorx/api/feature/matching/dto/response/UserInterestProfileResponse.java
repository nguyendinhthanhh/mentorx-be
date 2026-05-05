package com.mentorx.api.feature.matching.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserInterestProfileResponse(
    UUID id,
    UUID userId,
    String userFullName,
    Integer categoryId,
    String categoryName,
    BigDecimal interestScore,
    Integer interactionCount,
    Integer timeSpentMinutes,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime lastInteractionAt,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime lastUpdated,
    
    BigDecimal decayFactor,
    Boolean isExplicit,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt
) {}
