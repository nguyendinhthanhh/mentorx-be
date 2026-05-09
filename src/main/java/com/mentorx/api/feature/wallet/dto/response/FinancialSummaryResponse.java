package com.mentorx.api.feature.wallet.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FinancialSummaryResponse {
    private BigDecimal totalCirculation;
    private BigDecimal totalDepositToday;
    private BigDecimal totalWithdrawToday;
    private BigDecimal balanceDelta;
    private Long pendingWithdrawals;
    private Long unmatchedDeposits;
    private BigDecimal totalUnmatchedAmount;
    private Long fraudAlerts;
    private Long frozenAccountCount;
    private Double frozenRatio;
    private java.time.LocalDateTime lastReconciledAt;
    private Integer integrityScore; // 0-100
}
