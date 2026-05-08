package com.mentorx.api.feature.feed.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for course recommendations with match score
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRecommendationResponse {
    
    private UUID courseId;
    private String title;
    private String slug;
    private String description;
    private String thumbnailUrl;
    private BigDecimal price;
    private String instructorName;
    private UUID instructorId;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer totalEnrollments;
    private Integer totalDurationMinutes;
    private Integer totalLessons;
    private String level;
    private String language;
    private List<String> skills;
    private Integer categoryId;
    private String categoryName;
    private BigDecimal matchScore;
    private Boolean isCertificate;
}
