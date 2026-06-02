package com.mentorx.api.feature.job.repository;

import com.mentorx.api.feature.job.entity.Proposal;
import com.mentorx.api.feature.job.enums.ProposalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Proposal p WHERE p.id = :proposalId")
    Optional<Proposal> findByIdForUpdate(@Param("proposalId") UUID proposalId);

    List<Proposal> findByJobId(UUID jobId);
    Page<Proposal> findByJobId(UUID jobId, Pageable pageable);
    Page<Proposal> findByMentorId(UUID mentorId, Pageable pageable);
    Page<Proposal> findByJobIdAndStatus(UUID jobId, ProposalStatus status, Pageable pageable);
    Optional<Proposal> findByJobIdAndMentorId(UUID jobId, UUID mentorId);
}
