package com.mentorx.api.feature.wallet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_balance_audit_log")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletBalanceAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "old_balance_mxc", nullable = false, precision = 15, scale = 4)
    private BigDecimal oldBalanceMxc;

    @Column(name = "new_balance_mxc", nullable = false, precision = 15, scale = 4)
    private BigDecimal newBalanceMxc;

    @Column(name = "delta_mxc", nullable = false, precision = 15, scale = 4)
    private BigDecimal deltaMxc;

    @Column(name = "old_version", nullable = false)
    private Long oldVersion;

    @Column(name = "new_version", nullable = false)
    private Long newVersion;

    @CreatedDate
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by_txn")
    private UUID changedByTxn;
}
