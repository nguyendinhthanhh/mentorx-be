package com.mentorx.api.feature.wallet.repository;

import com.mentorx.api.feature.wallet.entity.DepositOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepositOrderRepository extends JpaRepository<DepositOrder, UUID> {
    Optional<DepositOrder> findByGatewayAndGatewayOrderId(String gateway, String gatewayOrderId);
}
