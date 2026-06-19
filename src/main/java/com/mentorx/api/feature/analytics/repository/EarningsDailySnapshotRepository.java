package com.mentorx.api.feature.analytics.repository;

import com.mentorx.api.feature.analytics.entity.EarningsDailySnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EarningsDailySnapshotRepository extends JpaRepository<EarningsDailySnapshot, UUID> {

    Page<EarningsDailySnapshot> findByUserIdOrderBySnapshotDateDesc(UUID userId, Pageable pageable);

    List<EarningsDailySnapshot> findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            UUID userId, LocalDate start, LocalDate end);

    Optional<EarningsDailySnapshot> findByUserIdAndSnapshotDate(UUID userId, LocalDate snapshotDate);

    /**
     * Returns one row per snapshot_date in the range, ordered ascending.
     * Used by the daily-chart timeline endpoint.
     */
    @Query("""
        SELECT s FROM EarningsDailySnapshot s
        WHERE s.user.id = :userId
          AND s.snapshotDate BETWEEN :start AND :end
        ORDER BY s.snapshotDate ASC
    """)
    List<EarningsDailySnapshot> findTimeline(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}