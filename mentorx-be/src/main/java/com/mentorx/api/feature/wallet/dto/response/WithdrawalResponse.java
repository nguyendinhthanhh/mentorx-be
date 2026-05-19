package com.mentorx.api.feature.wallet.dto.response;

import com.mentorx.api.common.enums.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WithdrawalResponse(
        UUID id,
        UUID userId,
        BigDecimal mxcAmount,
        BigDecimal feeMxc,
        BigDecimal netMxc,
        BigDecimal realAmount,
        String realCurrency,
        BigDecimal exchangeRate,
        String bankName,
        String bankAccountNo,
        String bankAccountName,
        WithdrawalStatus status,
        String gatewayTxnId,
        LocalDateTime reviewedAt,
        LocalDateTime payoutAt,
        LocalDateTime createdAt
) {
}
