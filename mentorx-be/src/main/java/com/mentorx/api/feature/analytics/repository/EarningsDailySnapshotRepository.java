package com.mentorx.api.feature.analytics.repository;

import com.mentorx.api.feature.analytics.entity.EarningsDailySnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EarningsDailySnapshotRepository extends JpaRepository<EarningsDailySnapshot, UUID> {
    Page<EarningsDailySnapshot> findByUserIdOrderBySnapshotDateDesc(UUID userId, Pageable pageable);
}
