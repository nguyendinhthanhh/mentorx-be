package com.mentorx.api.feature.wallet.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private WalletAccountType accountType;

    @Column(name = "balance_mxc", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal balanceMxc = BigDecimal.ZERO;

    @Column(name = "ledger_hash", length = 64)
    private String ledgerHash;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Relationships
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WalletTransaction> transactions;

    public void addToBalance(BigDecimal amount) {
        this.balanceMxc = this.balanceMxc.add(amount);
    }

    public void subtractFromBalance(BigDecimal amount) {
        this.balanceMxc = this.balanceMxc.subtract(amount);
    }
}
