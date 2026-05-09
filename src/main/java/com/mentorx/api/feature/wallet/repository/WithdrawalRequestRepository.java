package com.mentorx.api.feature.wallet.repository;

import com.mentorx.api.feature.wallet.entity.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, UUID> {
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(w.mxcAmount), 0) FROM WithdrawalRequest w WHERE w.createdAt >= :startOfDay AND w.status = 'COMPLETED'")
    java.math.BigDecimal getTodayTotalWithdrawals(@org.springframework.data.repository.query.Param("startOfDay") java.time.LocalDateTime startOfDay);
}
