package com.mentorx.api.feature.wallet.repository;

import com.mentorx.api.feature.wallet.entity.DepositOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepositOrderRepository extends JpaRepository<DepositOrder, UUID> {
    Optional<DepositOrder> findByGatewayAndGatewayOrderId(String gateway, String gatewayOrderId);

    Optional<DepositOrder> findByGatewayOrderId(String gatewayOrderId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(d.mxcAmount), 0) FROM DepositOrder d WHERE d.createdAt >= :startOfDay AND d.txnStatus = 'COMPLETED'")
    java.math.BigDecimal getTodayTotalDeposits(@org.springframework.data.repository.query.Param("startOfDay") java.time.LocalDateTime startOfDay);
}
