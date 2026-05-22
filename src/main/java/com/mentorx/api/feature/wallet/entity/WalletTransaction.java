package com.mentorx.api.feature.wallet.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.LedgerDirection;
import com.mentorx.api.common.enums.TxnStatus;
import com.mentorx.api.common.enums.TxnType;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    @Column(name = "amount_mxc", nullable = false, precision = 15, scale = 4)
    private BigDecimal amountMxc;

    @Column(name = "balance_after_mxc", nullable = false, precision = 15, scale = 4)
    private BigDecimal balanceAfterMxc;

    @Column(name = "original_amount", precision = 19, scale = 6)
    private BigDecimal originalAmount;

    @Column(name = "original_currency", length = 10)
    private String originalCurrency;

    @Column(name = "exchange_rate_to_vnd", precision = 19, scale = 6)
    private BigDecimal exchangeRateToVnd;

    @Column(name = "converted_amount_vnd", precision = 19, scale = 2)
    private BigDecimal convertedAmountVnd;

    @Column(name = "gateway", length = 30)
    private String gateway;

    @Column(name = "gateway_transaction_id", length = 255)
    private String gatewayTransactionId;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_status", nullable = false)
    @Builder.Default
    private TxnStatus txnStatus = TxnStatus.COMPLETED;

    @Column(name = "entry_hash", nullable = false, length = 64)
    private String entryHash;

    @Column(name = "prev_entry_hash", length = 64)
    private String prevEntryHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
