package com.mentorx.api.feature.feed.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for personalized feed
 * Aggregates all recommendation types (mentors, courses, jobs)
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalizedFeedResponse {
    
    /**
     * List of recommended mentors with match scores
     */
    private List<MentorRecommendationResponse> mentors;
    
    /**
     * List of recommended courses with match scores
     */
    private List<CourseRecommendationResponse> courses;
    
    /**
     * List of recommended jobs with match scores
     */
    private List<JobRecommendationResponse> jobs;
    
    /**
     * Timestamp when this feed was generated
     */
    private LocalDateTime generatedAt;
    
    /**
     * Source of the feed data (CACHE, DATABASE, REAL_TIME)
     */
    private String source;
    
    /**
     * Total number of recommendations in the feed
     */
    private Integer totalItems;
    
    /**
     * Whether this feed was computed in real-time
     */
    private Boolean isRealTime;
}
