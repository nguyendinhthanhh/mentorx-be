package com.mentorx.api.feature.wallet.dto.response;

import com.mentorx.api.common.enums.WalletAccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        UUID userId,
        String userFullName,
        WalletAccountType accountType,
        BigDecimal balance,
        BigDecimal pendingBalance,
        BigDecimal availableBalance,
        String lastTransactionHash,
        Boolean isActive,
        Boolean isFrozen,
        LocalDateTime frozenAt,
        String frozenReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}