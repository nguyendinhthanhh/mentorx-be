package com.mentorx.api.feature.feed.service;

import com.mentorx.api.feature.feed.dto.response.MentorRecommendationResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for mentor recommendations
 * Provides personalized mentor suggestions based on user interests and skills
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
public interface MentorRecommendationService {

    /**
     * Get personalized mentor recommendations for a user
     * Returns mentors with match scores >= 85%, sorted by match score descending
     * 
     * @param userId user ID
     * @param limit maximum number of recommendations to return
     * @return list of mentor recommendations with match scores
     */
    List<MentorRecommendationResponse> getRecommendedMentors(UUID userId, int limit);

    /**
     * Get all mentor recommendations for a user (no limit)
     * 
     * @param userId user ID
     * @return list of all mentor recommendations with match scores >= 85%
     */
    List<MentorRecommendationResponse> getRecommendedMentors(UUID userId);

    /**
     * Calculate match score for a specific mentor and user
     * 
     * @param userId user ID
     * @param mentorId mentor ID
     * @return mentor recommendation with match score
     */
    MentorRecommendationResponse calculateMentorMatch(UUID userId, UUID mentorId);
}
