package com.mentorx.api.feature.wallet.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private WalletAccountType accountType;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "pending_balance", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal pendingBalance = BigDecimal.ZERO;

    @Column(name = "available_balance", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(name = "last_transaction_hash", length = 64)
    private String lastTransactionHash;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_frozen", nullable = false)
    @Builder.Default
    private Boolean isFrozen = false;

    @Column(name = "frozen_at")
    private LocalDateTime frozenAt;

    @Column(name = "frozen_reason", columnDefinition = "TEXT")
    private String frozenReason;

    // Relationships
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WalletTransaction> transactions;

    // Helper methods for balance calculations
    public void addToBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
    }

    public void subtractFromBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
    }

    public void addToPendingBalance(BigDecimal amount) {
        this.pendingBalance = this.pendingBalance.add(amount);
    }

    public void subtractFromPendingBalance(BigDecimal amount) {
        this.pendingBalance = this.pendingBalance.subtract(amount);
    }

    public void movePendingToAvailable(BigDecimal amount) {
        this.pendingBalance = this.pendingBalance.subtract(amount);
        this.availableBalance = this.availableBalance.add(amount);
    }
}