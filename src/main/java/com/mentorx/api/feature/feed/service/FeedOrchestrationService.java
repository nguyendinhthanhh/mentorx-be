package com.mentorx.api.feature.feed.service;

import com.mentorx.api.feature.feed.dto.response.PersonalizedFeedResponse;

import java.util.UUID;

/**
 * Service interface for orchestrating personalized feed generation
 * Implements cache-first strategy with fallbacks to database and real-time computation
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
public interface FeedOrchestrationService {

    /**
     * Get personalized feed for a user
     * Strategy:
     * 1. Check Redis cache (fastest)
     * 2. Fallback to database precomputed feed items
     * 3. Fallback to real-time computation via recommendation services
     * 
     * @param userId user ID
     * @return personalized feed with all recommendation types
     */
    PersonalizedFeedResponse getPersonalizedFeed(UUID userId);

    /**
     * Get personalized feed with custom limits per section
     * 
     * @param userId user ID
     * @param mentorLimit maximum number of mentor recommendations
     * @param courseLimit maximum number of course recommendations
     * @param jobLimit maximum number of job recommendations
     * @return personalized feed with limited recommendations
     */
    PersonalizedFeedResponse getPersonalizedFeed(UUID userId, int mentorLimit, int courseLimit, int jobLimit);

    /**
     * Precompute and store feed items for a user
     * Used by background jobs to refresh feed data
     * 
     * @param userId user ID
     */
    void precomputeFeedForUser(UUID userId);

    /**
     * Invalidate cached feed for a user
     * Forces fresh computation on next request
     * 
     * @param userId user ID
     */
    void invalidateUserFeed(UUID userId);

    /**
     * Invalidate all cached feeds
     * Used after background job completes
     */
    void invalidateAllFeeds();
}
