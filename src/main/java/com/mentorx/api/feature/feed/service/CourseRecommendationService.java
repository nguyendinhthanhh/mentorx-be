package com.mentorx.api.feature.feed.service;

import com.mentorx.api.feature.feed.dto.response.CourseRecommendationResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for course recommendations
 * Provides personalized course suggestions based on user interests, skill level, and categories
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
public interface CourseRecommendationService {

    /**
     * Get personalized course recommendations for a user
     * Returns courses with match scores >= 85%, sorted by match score descending
     * Filters by both skill level AND interest categories
     * 
     * @param userId user ID
     * @param limit maximum number of recommendations to return
     * @return list of course recommendations with match scores
     */
    List<CourseRecommendationResponse> getRecommendedCourses(UUID userId, int limit);

    /**
     * Get all course recommendations for a user (no limit)
     * 
     * @param userId user ID
     * @return list of all course recommendations with match scores >= 85%
     */
    List<CourseRecommendationResponse> getRecommendedCourses(UUID userId);

    /**
     * Calculate match score for a specific course and user
     * 
     * @param userId user ID
     * @param courseId course ID
     * @return course recommendation with match score
     */
    CourseRecommendationResponse calculateCourseMatch(UUID userId, UUID courseId);
}
