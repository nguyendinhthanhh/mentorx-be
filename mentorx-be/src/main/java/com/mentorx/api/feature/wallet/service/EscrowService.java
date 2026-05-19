package com.mentorx.api.feature.wallet.service;

import com.mentorx.api.feature.wallet.dto.response.EscrowRecordResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface EscrowService {

    /**
     * Luồng 2: Lock tiền client vào escrow khi accept proposal
     * CLIENT_USER_AVAILABLE → ESCROW
     * Tạo escrow_record + contract
     */
    EscrowRecordResponse lockEscrowForContract(UUID clientId, UUID contractId,
                                                BigDecimal totalAmountMxc, BigDecimal platformFeeMxc);

    /**
     * Luồng 3A: Client approve milestone → release escrow
     * ESCROW → PLATFORM_REVENUE (fee) + MENTOR_USER_PENDING (net)
     */
    EscrowRecordResponse releaseMilestoneEscrow(UUID contractId, UUID milestoneId,
                                                 BigDecimal milestoneAmount, BigDecimal platformFee,
                                                 UUID mentorId);

    /**
     * Luồng 6: Refund escrow → CLIENT_USER_AVAILABLE
     * Trả tiền còn trong escrow về client
     */
    EscrowRecordResponse refundEscrow(UUID contractId, UUID clientId, BigDecimal refundAmount);

    /**
     * Lấy tất cả escrow records của 1 contract
     */
    List<EscrowRecordResponse> getEscrowsByContract(UUID contractId);

    /**
     * Lấy 1 escrow record theo ID
     */
    EscrowRecordResponse getEscrowById(UUID escrowId);

    /**
     * Tổng tiền đang bị lock trong escrow cho 1 contract
     */
    BigDecimal getTotalLockedByContract(UUID contractId);

    /**
     * Tổng tiền đã release cho 1 contract
     */
    BigDecimal getTotalReleasedByContract(UUID contractId);

    /**
     * Tổng tiền đang bị lock trong toàn hệ thống
     */
    BigDecimal getTotalEscrowLocked();
}
