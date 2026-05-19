package com.mentorx.api.feature.wallet.repository;

import com.mentorx.api.feature.wallet.entity.WalletBalanceAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WalletBalanceAuditLogRepository extends JpaRepository<WalletBalanceAuditLog, UUID> {
    Page<WalletBalanceAuditLog> findByWalletId(UUID walletId, Pageable pageable);
}
