package com.mentorx.api.feature.analytics.repository;

import com.mentorx.api.feature.matching.entity.UserInteractionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only access to the partitioned {@code user_interaction_events} table for analytics
 * (Phase 5 conversion queries). We deliberately avoid the source-of-truth writes through
 * this repo — events are emitted from chat / job / course modules.
 */
@Repository
public interface UserInteractionEventRepository extends JpaRepository<UserInteractionEvent, Long> {

    long countByUserIdAndInteractionTypeAndInteractionTimestampBetween(
            UUID userId, String interactionType, LocalDateTime start, LocalDateTime end);

    @Query("SELECT DATE(e.interactionTimestamp) AS d, COUNT(e) " +
           "FROM UserInteractionEvent e " +
           "WHERE e.user.id = :userId " +
           "AND e.interactionType = :interactionType " +
           "AND e.interactionTimestamp BETWEEN :start AND :end " +
           "GROUP BY DATE(e.interactionTimestamp) " +
           "ORDER BY d ASC")
    Object[][] countByUserIdAndTypeGroupedByDay(
            @Param("userId") UUID userId,
            @Param("interactionType") String interactionType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
