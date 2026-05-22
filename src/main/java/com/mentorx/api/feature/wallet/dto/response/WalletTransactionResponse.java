package com.mentorx.api.feature.wallet.dto.response;

import com.mentorx.api.common.enums.LedgerDirection;
import com.mentorx.api.common.enums.TxnStatus;
import com.mentorx.api.common.enums.TxnType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletTransactionResponse(
        UUID id,
        UUID walletId,
        UUID transactionGroupId,
        TxnType txnType,
        LedgerDirection direction,
        BigDecimal originalAmount,
        String originalCurrency,
        BigDecimal exchangeRateToVnd,
        BigDecimal convertedAmountVnd,
        BigDecimal amountMxc,
        BigDecimal balanceAfterMxc,
        UUID referenceId,
        String referenceType,
        String note,
        TxnStatus txnStatus,
        String gateway,
        String gatewayTransactionId,
        String entryHash,
        String prevEntryHash,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
