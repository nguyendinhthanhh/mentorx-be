package com.mentorx.api.feature.matching.service;

import com.mentorx.api.feature.matching.entity.MentorMatchScore;
import com.mentorx.api.feature.matching.entity.PrecomputedFeedItem;
import com.mentorx.api.feature.matching.entity.UserInterestProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for matching and recommendation algorithms
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public interface MatchingService {

    /**
     * Get personalized mentor recommendations for a user
     */
    Page<MentorMatchScore> getMentorRecommendations(Long userId, Pageable pageable);

    /**
     * Get personalized feed items for a user
     */
    Page<PrecomputedFeedItem> getPersonalizedFeed(Long userId, Pageable pageable);

    /**
     * Update user interest profile based on interaction
     */
    void updateUserInterest(Long userId, Long categoryId, String interactionType, Integer durationMinutes);

    /**
     * Compute match score between user and mentor
     */
    BigDecimal computeMatchScore(Long userId, Long mentorId);

    /**
     * Recompute all match scores for a user
     */
    void recomputeUserMatches(Long userId);

    /**
     * Get user's top interests
     */
    List<UserInterestProfile> getUserTopInterests(Long userId, int limit);

    /**
     * Find similar users based on interests
     */
    List<Long> findSimilarUsers(Long userId, int limit);

    /**
     * Generate feed items for a user
     */
    void generateFeedItems(Long userId);

    /**
     * Clean up expired data
     */
    void cleanupExpiredData();
}