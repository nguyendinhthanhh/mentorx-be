package com.mentorx.api.feature.job.repository;

import com.mentorx.api.feature.job.entity.Milestone;
import com.mentorx.api.feature.job.enums.MilestoneStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {
    Page<Milestone> findByContractId(UUID contractId, Pageable pageable);
    List<Milestone> findByContractIdOrderByMilestoneOrderAsc(UUID contractId);
    Page<Milestone> findByStatus(MilestoneStatus status, Pageable pageable);
}
