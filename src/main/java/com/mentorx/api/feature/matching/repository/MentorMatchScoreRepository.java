package com.mentorx.api.feature.matching.repository;

import com.mentorx.api.feature.matching.entity.MentorMatchScore;
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
 * Repository interface for MentorMatchScore entity
 * Provides data access methods for mentor matching and recommendations
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Repository
public interface MentorMatchScoreRepository extends JpaRepository<MentorMatchScore, UUID> {

    /**
     * Find top mentor matches for a user
     */
    @Query("SELECT mms FROM MentorMatchScore mms WHERE mms.user.id = :userId " +
           "AND mms.expiresAt > :now ORDER BY mms.matchScore DESC")
    Page<MentorMatchScore> findTopMatchesForUser(@Param("userId") UUID userId,
                                                @Param("now") LocalDateTime now,
                                                Pageable pageable);

    /**
     * Find specific match score between user and mentor
     */
    Optional<MentorMatchScore> findByUserIdAndMentorProfileId(UUID userId, UUID mentorProfileId);

    /**
     * Find matches that haven't been shown to user yet
     */
    @Query("SELECT mms FROM MentorMatchScore mms WHERE mms.user.id = :userId " +
           "AND mms.isShown = false AND mms.expiresAt > :now " +
           "ORDER BY mms.matchScore DESC")
    Page<MentorMatchScore> findUnshownMatchesForUser(@Param("userId") UUID userId,
                                                    @Param("now") LocalDateTime now,
                                                    Pageable pageable);

    /**
     * Find matches with minimum score threshold
     */
    @Query("SELECT mms FROM MentorMatchScore mms WHERE mms.user.id = :userId " +
           "AND mms.matchScore >= :minScore AND mms.expiresAt > :now " +
           "ORDER BY mms.matchScore DESC")
    List<MentorMatchScore> findHighQualityMatches(@Param("userId") UUID userId,
                                                 @Param("minScore") BigDecimal minScore,
                                                 @Param("now") LocalDateTime now);

    /**
     * Find expired match scores for cleanup
     */
    @Query("SELECT mms FROM MentorMatchScore mms WHERE mms.expiresAt <= :now")
    List<MentorMatchScore> findExpiredMatches(@Param("now") LocalDateTime now);

    /**
     * Mark matches as shown
     */
    @Modifying
    @Query("UPDATE MentorMatchScore mms SET mms.isShown = true, " +
           "mms.shownAt = :shownAt, mms.showCount = mms.showCount + 1 " +
           "WHERE mms.id IN :matchIds")
    int markMatchesAsShown(@Param("matchIds") List<UUID> matchIds,
                          @Param("shownAt") LocalDateTime shownAt);

    /**
     * Find matches for a specific mentor
     */
    @Query("SELECT mms FROM MentorMatchScore mms WHERE mms.mentorProfile.id = :mentorId " +
           "AND mms.expiresAt > :now ORDER BY mms.matchScore DESC")
    Page<MentorMatchScore> findMatchesForMentor(@Param("mentorId") UUID mentorId,
                                               @Param("now") LocalDateTime now,
                                               Pageable pageable);

    /**
     * Get match statistics for a user
     */
    @Query("SELECT COUNT(*), AVG(mms.matchScore), MAX(mms.matchScore) " +
           "FROM MentorMatchScore mms WHERE mms.user.id = :userId " +
           "AND mms.expiresAt > :now")
    Object[] getMatchStatsForUser(@Param("userId") UUID userId,
                                 @Param("now") LocalDateTime now);

    /**
     * Find matches by compatibility criteria
     */
    @Query("SELECT mms FROM MentorMatchScore mms WHERE mms.user.id = :userId " +
           "AND mms.interestCompatibility >= :minInterest " +
           "AND mms.skillCompatibility >= :minSkill " +
           "AND mms.budgetCompatibility >= :minBudget " +
           "AND mms.expiresAt > :now " +
           "ORDER BY mms.matchScore DESC")
    List<MentorMatchScore> findMatchesByCompatibility(@Param("userId") UUID userId,
                                                     @Param("minInterest") BigDecimal minInterest,
                                                     @Param("minSkill") BigDecimal minSkill,
                                                     @Param("minBudget") BigDecimal minBudget,
                                                     @Param("now") LocalDateTime now);

    /**
     * Delete expired matches
     */
    @Modifying
    @Query("DELETE FROM MentorMatchScore mms WHERE mms.expiresAt <= :cutoffDate")
    int deleteExpiredMatches(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count matches for a user
     */
    long countByUserIdAndExpiresAtAfter(UUID userId, LocalDateTime now);

    /**
     * Find matches that need recomputation (old algorithm version)
     */
    @Query("SELECT mms FROM MentorMatchScore mms WHERE mms.algorithmVersion != :currentVersion " +
           "OR mms.algorithmVersion IS NULL")
    List<MentorMatchScore> findMatchesNeedingRecomputation(@Param("currentVersion") String currentVersion);

    /**
     * Get top mentors by average match score
     */
    @Query("SELECT mms.mentorProfile.id, AVG(mms.matchScore), COUNT(*) " +
           "FROM MentorMatchScore mms WHERE mms.expiresAt > :now " +
           "GROUP BY mms.mentorProfile.id " +
           "HAVING COUNT(*) >= :minMatches " +
           "ORDER BY AVG(mms.matchScore) DESC")
    List<Object[]> getTopMentorsByAverageScore(@Param("now") LocalDateTime now,
                                              @Param("minMatches") Long minMatches);

    /**
     * Find matches with high show count but no interaction
     */
    @Query("SELECT mms FROM MentorMatchScore mms WHERE mms.user.id = :userId " +
           "AND mms.showCount >= :minShows AND mms.isShown = true " +
           "AND mms.expiresAt > :now " +
           "ORDER BY mms.showCount DESC")
    List<MentorMatchScore> findOverexposedMatches(@Param("userId") UUID userId,
                                                 @Param("minShows") Integer minShows,
                                                 @Param("now") LocalDateTime now);

    /**
     * Update match score components
     */
    @Modifying
    @Query("UPDATE MentorMatchScore mms SET " +
           "mms.matchScore = :matchScore, " +
           "mms.interestCompatibility = :interestComp, " +
           "mms.skillCompatibility = :skillComp, " +
           "mms.budgetCompatibility = :budgetComp, " +
           "mms.availabilityCompatibility = :availComp, " +
           "mms.communicationCompatibility = :commComp, " +
           "mms.geographicCompatibility = :geoComp, " +
           "mms.algorithmVersion = :algorithmVersion, " +
           "mms.expiresAt = :expiresAt " +
           "WHERE mms.id = :matchId")
    int updateMatchScore(@Param("matchId") UUID matchId,
                        @Param("matchScore") BigDecimal matchScore,
                        @Param("interestComp") BigDecimal interestComp,
                        @Param("skillComp") BigDecimal skillComp,
                        @Param("budgetComp") BigDecimal budgetComp,
                        @Param("availComp") BigDecimal availComp,
                        @Param("commComp") BigDecimal commComp,
                        @Param("geoComp") BigDecimal geoComp,
                        @Param("algorithmVersion") String algorithmVersion,
                        @Param("expiresAt") LocalDateTime expiresAt);
}