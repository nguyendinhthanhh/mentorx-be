package com.mentorx.api.feature.feed.service;

import com.mentorx.api.feature.feed.dto.response.JobRecommendationResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for job recommendations
 * Provides personalized job suggestions based on user skills and budget range
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
public interface JobRecommendationService {

    /**
     * Get personalized job recommendations for a user
     * Returns jobs with match scores >= 85%, sorted by match score descending
     * Filters by user skills and appropriate budget range for skill level
     * 
     * @param userId user ID
     * @param limit maximum number of recommendations to return
     * @return list of job recommendations with match scores
     */
    List<JobRecommendationResponse> getRecommendedJobs(UUID userId, int limit);

    /**
     * Get all job recommendations for a user (no limit)
     * 
     * @param userId user ID
     * @return list of all job recommendations with match scores >= 85%
     */
    List<JobRecommendationResponse> getRecommendedJobs(UUID userId);

    /**
     * Calculate match score for a specific job and user
     * 
     * @param userId user ID
     * @param jobId job ID
     * @return job recommendation with match score
     */
    JobRecommendationResponse calculateJobMatch(UUID userId, UUID jobId);
}
