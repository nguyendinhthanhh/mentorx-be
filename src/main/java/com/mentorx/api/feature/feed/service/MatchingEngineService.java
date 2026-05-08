package com.mentorx.api.feature.feed.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for matching engine calculations
 * Implements the core matching algorithm for personalized recommendations
 * 
 * Formula: matchScore = (skillMatch * 0.6) + (levelMatch * 0.3) + (ratingBonus * 0.1)
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
public interface MatchingEngineService {

    /**
     * Calculate match score between user interests and content item
     * 
     * @param userId user ID
     * @param itemSkills skills associated with the content item
     * @param itemLevel skill level of the content (Beginner, Intermediate, Advanced)
     * @param itemRating rating of the content (0.0 - 5.0)
     * @param itemCategoryId category ID of the content
     * @return match score percentage (0.00 - 100.00)
     */
    BigDecimal calculateMatchScore(
        UUID userId,
        Set<String> itemSkills,
        String itemLevel,
        BigDecimal itemRating,
        Integer itemCategoryId
    );

    /**
     * Calculate skill match component
     * Awards 20 points for each matching skill
     * 
     * @param userSkills user's skills
     * @param itemSkills item's required/related skills
     * @return skill match score (0 - unlimited, typically 0-100)
     */
    BigDecimal calculateSkillMatch(Set<String> userSkills, Set<String> itemSkills);

    /**
     * Calculate level match component
     * Awards 15 points if user's level matches item's level
     * 
     * @param userLevel user's skill level
     * @param itemLevel item's skill level
     * @return level match score (0 or 15)
     */
    BigDecimal calculateLevelMatch(String userLevel, String itemLevel);

    /**
     * Calculate rating bonus component
     * Awards up to 10 points based on item rating
     * Formula: (rating / 5) * 10
     * 
     * @param rating item rating (0.0 - 5.0)
     * @return rating bonus score (0.00 - 10.00)
     */
    BigDecimal calculateRatingBonus(BigDecimal rating);

    /**
     * Check if user is interested in a category
     * 
     * @param userId user ID
     * @param categoryId category ID
     * @return true if user has interest in the category
     */
    boolean isInterestedInCategory(UUID userId, Integer categoryId);

    /**
     * Get user's skill level from their interest profile
     * Returns the most common level or "Intermediate" as default
     * 
     * @param userId user ID
     * @return skill level (Beginner, Intermediate, or Advanced)
     */
    String getUserSkillLevel(UUID userId);

    /**
     * Get user's skills from their profile
     * 
     * @param userId user ID
     * @return set of skill names
     */
    Set<String> getUserSkills(UUID userId);

    /**
     * Get user's interested category IDs
     * 
     * @param userId user ID
     * @return list of category IDs user is interested in
     */
    List<Integer> getUserInterestedCategories(UUID userId);

    /**
     * Filter items by minimum match score threshold
     * Default threshold is 85%
     * 
     * @param items list of items with match scores
     * @param threshold minimum match score threshold
     * @param <T> type of item
     * @return filtered list of items above threshold
     */
    <T> List<T> filterByThreshold(List<T> items, BigDecimal threshold);

    /**
     * Sort items by match score in descending order
     * 
     * @param items list of items with match scores
     * @param <T> type of item
     * @return sorted list of items
     */
    <T> List<T> sortByMatchScore(List<T> items);
}
