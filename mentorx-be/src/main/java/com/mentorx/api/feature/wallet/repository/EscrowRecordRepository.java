package com.mentorx.api.feature.wallet.repository;

import com.mentorx.api.common.enums.EscrowStatus;
import com.mentorx.api.feature.wallet.entity.EscrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EscrowRecordRepository extends JpaRepository<EscrowRecord, UUID> {

    List<EscrowRecord> findByContractId(UUID contractId);

    List<EscrowRecord> findByContractIdAndStatus(UUID contractId, EscrowStatus status);

    Optional<EscrowRecord> findByMilestoneId(UUID milestoneId);

    Optional<EscrowRecord> findByContractIdAndMilestoneIdIsNull(UUID contractId);

    @Query("SELECT COALESCE(SUM(e.lockedAmountMxc), 0) FROM EscrowRecord e WHERE e.contract.id = :contractId AND e.status = 'LOCKED'")
    BigDecimal getTotalLockedByContract(@Param("contractId") UUID contractId);

    @Query("SELECT COALESCE(SUM(e.lockedAmountMxc), 0) FROM EscrowRecord e WHERE e.contract.id = :contractId AND e.status = 'RELEASED'")
    BigDecimal getTotalReleasedByContract(@Param("contractId") UUID contractId);

    @Query("SELECT COALESCE(SUM(e.lockedAmountMxc), 0) FROM EscrowRecord e WHERE e.status = 'LOCKED'")
    BigDecimal getTotalEscrowLocked();

    List<EscrowRecord> findByStatus(EscrowStatus status);
}
