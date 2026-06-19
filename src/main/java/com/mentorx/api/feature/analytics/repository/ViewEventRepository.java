package com.mentorx.api.feature.analytics.repository;

import com.mentorx.api.feature.analytics.entity.ViewEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ViewEventRepository extends JpaRepository<ViewEvent, UUID> {

    long countByTargetTypeAndTargetId(String targetType, UUID targetId);

    /**
     * M12.2 H0: dedup query (DEC-006 Option B). Returns the most recent view event
     * matching (targetType, targetId, viewer-id OR ipAddress) within the dedup window.
     * The service checks the returned event's {@code createdAt} against the 1h window.
     */
    @Query("""
        SELECT v FROM ViewEvent v
        WHERE v.targetType = :targetType
          AND v.targetId   = :targetId
          AND ((:viewerId IS NOT NULL AND v.viewer.id = :viewerId)
               OR (:viewerId IS NULL AND v.ipAddress = :ipAddress))
        ORDER BY v.createdAt DESC
    """)
    List<ViewEvent> findRecentForDedup(@Param("targetType") String targetType,
                                      @Param("targetId") UUID targetId,
                                      @Param("viewerId") UUID viewerId,
                                      @Param("ipAddress") String ipAddress,
                                      org.springframework.data.domain.Pageable pageable);

    default Optional<ViewEvent> findLatestForDedup(String targetType,
                                                   UUID targetId,
                                                   UUID viewerId,
                                                   String ipAddress) {
        List<ViewEvent> rows = findRecentForDedup(targetType, targetId, viewerId, ipAddress,
                org.springframework.data.domain.PageRequest.of(0, 1));
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * M12.2 H0: count distinct viewers for a target, ignoring nulls (anonymous views).
     */
    @Query("SELECT COUNT(DISTINCT v.viewer.id) FROM ViewEvent v " +
           "WHERE v.targetType = :targetType AND v.targetId = :targetId AND v.viewer.id IS NOT NULL")
    long countUniqueViewersByTarget(@Param("targetType") String targetType,
                                    @Param("targetId") UUID targetId);

    /**
     * M12.2 H0: bulk delete of rows older than the cutoff timestamp.
     * Used by {@code AnalyticsRetentionJob} at 03:00 daily (DEC-010).
     */
    @Modifying
    @Query("DELETE FROM ViewEvent v WHERE v.createdAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);

    /**
     * M12.2 H0: count rows created after a given timestamp (diagnostic / verification helper).
     */
    long countByCreatedAtAfter(LocalDateTime cutoff);

    /**
     * M12.2 H0: per-day aggregation of view counts and unique viewers for a target.
     * Result rows: [date(java.sql.Date), total_views, unique_viewers].
     */
    @Query(value = """
        SELECT DATE_TRUNC('day', created_at) AS bucket,
               COUNT(*) AS total,
               COUNT(DISTINCT viewer_id) AS unique_count
        FROM view_events
        WHERE target_type = :targetType
          AND target_id   = :targetId
          AND created_at >= :start
          AND created_at <  :end
        GROUP BY DATE_TRUNC('day', created_at)
        ORDER BY bucket ASC
    """, nativeQuery = true)
    List<Object[]> aggregateByDay(@Param("targetType") String targetType,
                                  @Param("targetId") UUID targetId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}