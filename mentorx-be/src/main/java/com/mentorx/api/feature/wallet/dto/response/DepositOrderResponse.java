package com.mentorx.api.feature.wallet.dto.response;

import com.mentorx.api.common.enums.TxnStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DepositOrderResponse(
        UUID id,
        UUID userId,
        String gateway,
        String gatewayOrderId,
        BigDecimal realAmount,
        String realCurrency,
        BigDecimal mxcAmount,
        BigDecimal exchangeRate,
        TxnStatus txnStatus,
        LocalDateTime reconciledAt,
        LocalDateTime createdAt
) {
}
