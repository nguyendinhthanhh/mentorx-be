package com.mentorx.api.feature.wallet.dto.response;

import com.mentorx.api.common.enums.EscrowStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EscrowRecordResponse(
        UUID id,
        UUID contractId,
        UUID milestoneId,
        BigDecimal lockedAmountMxc,
        BigDecimal platformFeeMxc,
        BigDecimal mentorNetMxc,
        EscrowStatus status,
        LocalDateTime lockedAt,
        LocalDateTime releasedAt,
        UUID releasedToUserId,
        String releasedToFullName,
        UUID releaseTxnGroupId
) {
}
