package com.mentorx.api.feature.wallet.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.LedgerDirection;
import com.mentorx.api.common.enums.TxnStatus;
import com.mentorx.api.common.enums.TxnType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "transaction_group_id", nullable = false)
    private UUID transactionGroupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", nullable = false)
    private TxnType txnType;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private LedgerDirection direction;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "reference_id")
    private UUID referenceId; // Job ID, Contract ID, etc.

    @Column(name = "reference_type", length = 50)
    private String referenceType; // JOB, CONTRACT, WITHDRAWAL, etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TxnStatus status = TxnStatus.PENDING;

    @Column(name = "entry_hash", nullable = false, length = 64)
    private String entryHash;

    @Column(name = "prev_hash", length = 64)
    private String prevHash;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "external_txn_id", length = 100)
    private String externalTxnId; // Payment gateway transaction ID

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON for additional data
}