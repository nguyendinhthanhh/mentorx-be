package com.mentorx.api.feature.wallet.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.PaymentGateway;
import com.mentorx.api.common.enums.WithdrawalStatus;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawal_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "mxc_amount", nullable = false, precision = 15, scale = 4)
    private BigDecimal mxcAmount;

    @Column(name = "fee_mxc", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal feeMxc = BigDecimal.ZERO;

    @Column(name = "net_mxc", nullable = false, precision = 15, scale = 4)
    private BigDecimal netMxc;

    @Column(name = "real_amount", precision = 15, scale = 2)
    private BigDecimal realAmount;

    @Column(name = "real_currency", nullable = false, length = 3)
    @Builder.Default
    private String realCurrency = "VND";

    @Column(name = "exchange_rate", precision = 15, scale = 4)
    private BigDecimal exchangeRate;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_no", length = 50)
    private String bankAccountNo;

    @Column(name = "bank_account_name", length = 150)
    private String bankAccountName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WithdrawalStatus status = WithdrawalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    private PaymentGateway gateway;

    @Column(name = "gateway_txn_id", length = 255)
    private String gatewayTxnId;

    @Column(name = "payout_at")
    private LocalDateTime payoutAt;
}
