package com.mentorx.api.feature.matching.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record UserInterestProfileRequest(
    @NotNull(message = "User ID is required")
    UUID userId,
    
    @NotNull(message = "Category ID is required")
    Integer categoryId,
    
    @NotNull(message = "Interest score is required")
    @DecimalMin(value = "0.0", message = "Interest score must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Interest score must not exceed 1.0")
    BigDecimal interestScore,
    
    Integer interactionCount,
    
    Integer timeSpentMinutes,
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    BigDecimal decayFactor,
    
    Boolean isExplicit
) {}
