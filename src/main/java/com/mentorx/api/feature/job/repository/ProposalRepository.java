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

    // M12.2 H0: methods required by JobStatsServiceImpl.getStats (mentor + client views)
    long countByMentorId(UUID mentorId);
    long countByMentorIdAndStatus(UUID mentorId, ProposalStatus status);

    @Query("SELECT COUNT(p) FROM Proposal p WHERE p.mentor.id = :mentorId " +
           "AND p.status NOT IN (com.mentorx.api.feature.job.enums.ProposalStatus.ACCEPTED, " +
           "                       com.mentorx.api.feature.job.enums.ProposalStatus.REJECTED, " +
           "                       com.mentorx.api.feature.job.enums.ProposalStatus.AUTO_CLOSED, " +
           "                       com.mentorx.api.feature.job.enums.ProposalStatus.WITHDRAWN, " +
           "                       com.mentorx.api.feature.job.enums.ProposalStatus.CONTRACT_CANCELLED)")
    long countPendingByMentorId(@Param("mentorId") UUID mentorId);

    @Query("SELECT COUNT(p) FROM Proposal p WHERE p.job.client.id = :clientId")
    long countProposalsForJobsByClientId(@Param("clientId") UUID clientId);

    // M12.2 H0: required by EarningsAggregationJob.aggregateEarnings
    @Query("SELECT p.mentor.id, COUNT(p) FROM Proposal p " +
           "WHERE p.createdAt >= :start AND p.createdAt < :end " +
           "GROUP BY p.mentor.id")
    List<Object[]> countProposalsByMentorInWindow(@Param("start") java.time.LocalDateTime start,
                                                  @Param("end") java.time.LocalDateTime end);

    @Query("SELECT p.mentor.id, COUNT(p) FROM Proposal p " +
           "WHERE p.status = com.mentorx.api.feature.job.enums.ProposalStatus.ACCEPTED " +
           "AND p.updatedAt >= :start AND p.updatedAt < :end " +
           "GROUP BY p.mentor.id")
    List<Object[]> countAcceptedProposalsByMentorInWindow(@Param("start") java.time.LocalDateTime start,
                                                          @Param("end") java.time.LocalDateTime end);
}
