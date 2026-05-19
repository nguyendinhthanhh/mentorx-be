package com.mentorx.api.feature.matching.repository;

import com.mentorx.api.feature.matching.entity.UserInterestProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

/**
 * Repository interface for UserInterestProfile entity
 * Provides data access methods for user interest profiling and matching
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Repository
public interface UserInterestProfileRepository extends JpaRepository<UserInterestProfile, UUID> {

    /**
     * Find all interest profiles for a specific user
     */
    List<UserInterestProfile> findByUserIdOrderByInterestScoreDesc(UUID userId);

    /**
     * Find interest profile for a specific user and category
     */
    Optional<UserInterestProfile> findByUserIdAndCategoryId(UUID userId, Integer categoryId);

    /**
     * Find top interests for a user with minimum score threshold
     */
    @Query("SELECT uip FROM UserInterestProfile uip WHERE uip.user.id = :userId " +
           "AND uip.interestScore >= :minScore ORDER BY uip.interestScore DESC")
    List<UserInterestProfile> findTopInterestsByUser(@Param("userId") UUID userId, 
                                                    @Param("minScore") BigDecimal minScore);

    /**
     * Find users interested in a specific category with minimum score
     */
    @Query("SELECT uip FROM UserInterestProfile uip WHERE uip.category.id = :categoryId " +
           "AND uip.interestScore >= :minScore ORDER BY uip.interestScore DESC")
    Page<UserInterestProfile> findUsersInterestedInCategory(@Param("categoryId") Integer categoryId,
                                                           @Param("minScore") BigDecimal minScore,
                                                           Pageable pageable);

    /**
     * Find profiles that need score decay (haven't been updated recently)
     */
    @Query("SELECT uip FROM UserInterestProfile uip WHERE uip.lastUpdated < :cutoffDate")
    List<UserInterestProfile> findProfilesNeedingDecay(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Update interaction count and last interaction time
     */
    @Modifying
    @Query("UPDATE UserInterestProfile uip SET " +
           "uip.interactionCount = uip.interactionCount + 1, " +
           "uip.lastInteractionAt = :interactionTime, " +
           "uip.lastUpdated = :updateTime " +
           "WHERE uip.user.id = :userId AND uip.category.id = :categoryId")
    int incrementInteractionCount(@Param("userId") UUID userId,
                                 @Param("categoryId") Integer categoryId,
                                 @Param("interactionTime") LocalDateTime interactionTime,
                                 @Param("updateTime") LocalDateTime updateTime);

    /**
     * Update time spent for a user-category combination
     */
    @Modifying
    @Query("UPDATE UserInterestProfile uip SET " +
           "uip.timeSpentMinutes = uip.timeSpentMinutes + :additionalMinutes, " +
           "uip.lastUpdated = :updateTime " +
           "WHERE uip.user.id = :userId AND uip.category.id = :categoryId")
    int addTimeSpent(@Param("userId") UUID userId,
                    @Param("categoryId") Integer categoryId,
                    @Param("additionalMinutes") Integer additionalMinutes,
                    @Param("updateTime") LocalDateTime updateTime);

    /**
     * Apply decay to interest scores
     */
    @Modifying
    @Query("UPDATE UserInterestProfile uip SET " +
           "uip.interestScore = uip.interestScore * uip.decayFactor, " +
           "uip.lastUpdated = :updateTime " +
           "WHERE uip.id IN :profileIds")
    int applyDecayToProfiles(@Param("profileIds") List<UUID> profileIds,
                            @Param("updateTime") LocalDateTime updateTime);

    /**
     * Find similar users based on interest overlap
     */
    @Query("SELECT uip2.user.id, COUNT(*) as commonInterests, " +
           "AVG(ABS(uip1.interestScore - uip2.interestScore)) as scoreDifference " +
           "FROM UserInterestProfile uip1 " +
           "JOIN UserInterestProfile uip2 ON uip1.category.id = uip2.category.id " +
           "WHERE uip1.user.id = :userId AND uip2.user.id != :userId " +
           "AND uip1.interestScore >= :minScore AND uip2.interestScore >= :minScore " +
           "GROUP BY uip2.user.id " +
           "HAVING COUNT(*) >= :minCommonInterests " +
           "ORDER BY commonInterests DESC, scoreDifference ASC")
    List<Object[]> findSimilarUsers(@Param("userId") UUID userId,
                                   @Param("minScore") BigDecimal minScore,
                                   @Param("minCommonInterests") Long minCommonInterests);

    /**
     * Get category interest statistics
     */
    @Query("SELECT uip.category.id, COUNT(*), AVG(uip.interestScore), MAX(uip.interestScore) " +
           "FROM UserInterestProfile uip " +
           "WHERE uip.interestScore >= :minScore " +
           "GROUP BY uip.category.id " +
           "ORDER BY COUNT(*) DESC")
    List<Object[]> getCategoryInterestStats(@Param("minScore") BigDecimal minScore);

    /**
     * Find profiles by explicit vs inferred interest
     */
    List<UserInterestProfile> findByUserIdAndIsExplicit(UUID userId, Boolean isExplicit);

    /**
     * Count total profiles for a user
     */
    long countByUserId(UUID userId);

    /**
     * Find profiles with recent interactions
     */
    @Query("SELECT uip FROM UserInterestProfile uip WHERE uip.user.id = :userId " +
           "AND uip.lastInteractionAt >= :since ORDER BY uip.lastInteractionAt DESC")
    List<UserInterestProfile> findRecentInteractionsByUser(@Param("userId") UUID userId,
                                                          @Param("since") LocalDateTime since);

    /**
     * Delete old profiles with very low scores
     */
    @Modifying
    @Query("DELETE FROM UserInterestProfile uip WHERE uip.interestScore < :threshold " +
           "AND uip.lastUpdated < :cutoffDate AND uip.isExplicit = false")
    int deleteStaleProfiles(@Param("threshold") BigDecimal threshold,
                           @Param("cutoffDate") LocalDateTime cutoffDate);
}