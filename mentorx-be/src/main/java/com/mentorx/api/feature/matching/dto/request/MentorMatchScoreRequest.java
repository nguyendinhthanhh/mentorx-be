package com.mentorx.api.feature.matching.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record MentorMatchScoreRequest(
    @NotNull(message = "User ID is required")
    UUID userId,
    
    @NotNull(message = "Mentor profile ID is required")
    UUID mentorProfileId,
    
    @NotNull(message = "Match score is required")
    @DecimalMin(value = "0.0", message = "Match score must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Match score must not exceed 1.0")
    BigDecimal matchScore,
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    BigDecimal interestCompatibility,
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    BigDecimal skillCompatibility,
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    BigDecimal budgetCompatibility,
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    BigDecimal availabilityCompatibility,
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    BigDecimal communicationCompatibility,
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    BigDecimal geographicCompatibility,
    
    String algorithmVersion
) {}
