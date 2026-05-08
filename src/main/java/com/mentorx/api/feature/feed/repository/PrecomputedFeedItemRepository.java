package com.mentorx.api.feature.feed.repository;

import com.mentorx.api.common.enums.FeedItemType;
import com.mentorx.api.feature.feed.entity.PrecomputedFeedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for PrecomputedFeedItem entity
 * Provides methods for querying and managing precomputed feed items
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Repository
public interface PrecomputedFeedItemRepository extends JpaRepository<PrecomputedFeedItem, UUID> {

    /**
     * Find all valid (non-expired) feed items for a user, ordered by match score
     * 
     * @param userId the user ID
     * @param now current timestamp for expiration check
     * @return list of valid feed items sorted by match score descending
     */
    @Query("SELECT f FROM PersonalizedFeedItem f " +
           "WHERE f.user.id = :userId " +
           "AND f.expiresAt > :now " +
           "ORDER BY f.matchScore DESC")
    List<PrecomputedFeedItem> findValidFeedItemsByUserId(
        @Param("userId") UUID userId,
        @Param("now") LocalDateTime now
    );

    /**
     * Find valid feed items for a user filtered by item type
     * 
     * @param userId the user ID
     * @param itemType the feed item type
     * @param now current timestamp for expiration check
     * @return list of valid feed items of specified type sorted by match score descending
     */
    @Query("SELECT f FROM PersonalizedFeedItem f " +
           "WHERE f.user.id = :userId " +
           "AND f.itemType = :itemType " +
           "AND f.expiresAt > :now " +
           "ORDER BY f.matchScore DESC")
    List<PrecomputedFeedItem> findValidFeedItemsByUserIdAndType(
        @Param("userId") UUID userId,
        @Param("itemType") FeedItemType itemType,
        @Param("now") LocalDateTime now
    );

    /**
     * Find valid feed items for a user with minimum match score threshold
     * 
     * @param userId the user ID
     * @param minScore minimum match score threshold
     * @param now current timestamp for expiration check
     * @return list of valid feed items above threshold sorted by match score descending
     */
    @Query("SELECT f FROM PersonalizedFeedItem f " +
           "WHERE f.user.id = :userId " +
           "AND f.matchScore >= :minScore " +
           "AND f.expiresAt > :now " +
           "ORDER BY f.matchScore DESC")
    List<PrecomputedFeedItem> findValidFeedItemsByUserIdAndMinScore(
        @Param("userId") UUID userId,
        @Param("minScore") Double minScore,
        @Param("now") LocalDateTime now
    );

    /**
     * Check if valid feed items exist for a user
     * 
     * @param userId the user ID
     * @param now current timestamp for expiration check
     * @return true if valid feed items exist, false otherwise
     */
    @Query("SELECT COUNT(f) > 0 FROM PersonalizedFeedItem f " +
           "WHERE f.user.id = :userId " +
           "AND f.expiresAt > :now")
    boolean existsValidFeedItemsByUserId(
        @Param("userId") UUID userId,
        @Param("now") LocalDateTime now
    );

    /**
     * Delete all feed items for a user (used when recalculating feed)
     * 
     * @param userId the user ID
     */
    @Modifying
    @Query("DELETE FROM PersonalizedFeedItem f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Delete all expired feed items (cleanup job)
     * 
     * @param now current timestamp
     * @return number of deleted items
     */
    @Modifying
    @Query("DELETE FROM PersonalizedFeedItem f WHERE f.expiresAt <= :now")
    int deleteExpiredItems(@Param("now") LocalDateTime now);

    /**
     * Count valid feed items for a user
     * 
     * @param userId the user ID
     * @param now current timestamp for expiration check
     * @return count of valid feed items
     */
    @Query("SELECT COUNT(f) FROM PersonalizedFeedItem f " +
           "WHERE f.user.id = :userId " +
           "AND f.expiresAt > :now")
    long countValidFeedItemsByUserId(
        @Param("userId") UUID userId,
        @Param("now") LocalDateTime now
    );

    /**
     * Find all users who have expired feed items (for background job)
     * 
     * @param now current timestamp
     * @return list of user IDs with expired or no feed items
     */
    @Query("SELECT DISTINCT u.id FROM User u " +
           "WHERE u.id NOT IN (" +
           "  SELECT f.user.id FROM PersonalizedFeedItem f " +
           "  WHERE f.expiresAt > :now" +
           ")")
    List<UUID> findUsersNeedingFeedRecalculation(@Param("now") LocalDateTime now);
}
