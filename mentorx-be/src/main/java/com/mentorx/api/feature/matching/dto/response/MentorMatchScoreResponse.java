package com.mentorx.api.feature.matching.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record MentorMatchScoreResponse(
    UUID id,
    UUID userId,
    String userFullName,
    UUID mentorProfileId,
    String mentorFullName,
    BigDecimal matchScore,
    BigDecimal interestCompatibility,
    BigDecimal skillCompatibility,
    BigDecimal budgetCompatibility,
    BigDecimal availabilityCompatibility,
    BigDecimal communicationCompatibility,
    BigDecimal geographicCompatibility,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime computedAt,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime expiresAt,
    
    String algorithmVersion,
    Boolean isShown,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime shownAt,
    
    Integer showCount,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt
) {}
