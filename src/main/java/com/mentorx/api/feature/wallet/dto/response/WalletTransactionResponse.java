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
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String description,
        UUID referenceId,
        String referenceType,
        TxnStatus status,
        String entryHash,
        String prevHash,
        LocalDateTime processedAt,
        LocalDateTime failedAt,
        String failureReason,
        String externalTxnId,
        String metadata,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}