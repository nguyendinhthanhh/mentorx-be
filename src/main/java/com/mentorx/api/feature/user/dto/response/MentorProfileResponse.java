package com.mentorx.api.feature.user.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MentorProfileResponse(
    UUID id,
    String headline,
    BigDecimal hourlyRateMxc,
    Short yearsOfExperience,
    String availability,
    Short responseTimeHours,
    Integer totalJobsDone,
    BigDecimal successRate,
    BigDecimal averageRating,
    Integer totalReviews,
    Boolean isFeatured,
    String cvUrl,
    String portfolioUrl,
    UUID approvedBy,
    String approvedByName,
    LocalDateTime approvedAt,
    String rejectionReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}