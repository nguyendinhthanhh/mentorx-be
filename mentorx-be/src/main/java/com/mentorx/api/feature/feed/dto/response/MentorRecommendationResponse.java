package com.mentorx.api.feature.feed.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for mentor recommendations with match score
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorRecommendationResponse {
    
    private UUID mentorId;
    private UUID userId;
    private String fullName;
    private String displayName;
    private String avatarUrl;
    private String headline;
    private BigDecimal hourlyRate;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer totalJobsDone;
    private BigDecimal successRate;
    private String availability;
    private Integer responseTimeHours;
    private List<String> skills;
    private List<String> categories;
    private BigDecimal matchScore;
    private Boolean isFeatured;
    private Boolean isAvailable;
}
