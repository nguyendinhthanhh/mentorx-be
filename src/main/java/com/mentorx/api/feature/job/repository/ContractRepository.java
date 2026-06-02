package com.mentorx.api.feature.job.repository;

import com.mentorx.api.feature.job.entity.Contract;
import com.mentorx.api.feature.job.enums.ContractStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Contract c WHERE c.id = :contractId")
    Optional<Contract> findByIdForUpdate(@Param("contractId") UUID contractId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Contract c WHERE c.job.id = :jobId AND c.status IN :statuses ORDER BY c.createdAt DESC")
    List<Contract> findByJobIdAndStatusInForUpdate(@Param("jobId") UUID jobId, @Param("statuses") Collection<ContractStatus> statuses);

    List<Contract> findByJobId(UUID jobId);
    Page<Contract> findByJobId(UUID jobId, Pageable pageable);
    Page<Contract> findByClientId(UUID clientId, Pageable pageable);
    Page<Contract> findByMentorId(UUID mentorId, Pageable pageable);
    Page<Contract> findByStatus(ContractStatus status, Pageable pageable);
    Optional<Contract> findByProposalId(UUID proposalId);
    boolean existsByJobIdAndStatusIn(UUID jobId, Collection<ContractStatus> statuses);
    Optional<Contract> findFirstByJobIdAndStatusIn(UUID jobId, Collection<ContractStatus> statuses);

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Contract c
        WHERE c.client.id = :clientId
          AND c.mentor.id = :mentorId
          AND c.status <> :excludedStatus
    """)
    boolean existsClientMentorRelationshipExcludingStatus(
        @Param("clientId") UUID clientId,
        @Param("mentorId") UUID mentorId,
        @Param("excludedStatus") ContractStatus excludedStatus
    );
}
