package com.mentorx.api.feature.wallet.entity;

import com.mentorx.api.common.enums.EscrowStatus;
import com.mentorx.api.feature.job.entity.Contract;
import com.mentorx.api.feature.job.entity.Milestone;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "escrow_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id")
    private Milestone milestone;

    @Column(name = "locked_amount_mxc", nullable = false, precision = 15, scale = 4)
    private BigDecimal lockedAmountMxc;

    @Column(name = "platform_fee_mxc", nullable = false, precision = 15, scale = 4)
    private BigDecimal platformFeeMxc;

    @Column(name = "mentor_net_mxc", nullable = false, precision = 15, scale = 4)
    private BigDecimal mentorNetMxc;

    @Column(name = "locked_at", nullable = false)
    @Builder.Default
    private LocalDateTime lockedAt = LocalDateTime.now();

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "released_to")
    private User releasedTo;

    @Column(name = "release_txn_group_id")
    private UUID releaseTxnGroupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EscrowStatus status = EscrowStatus.LOCKED;
}
