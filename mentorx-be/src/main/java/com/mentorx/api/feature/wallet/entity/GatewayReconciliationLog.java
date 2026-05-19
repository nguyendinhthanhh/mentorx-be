package com.mentorx.api.feature.wallet.entity;

import com.mentorx.api.common.enums.PaymentGateway;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gateway_reconciliation_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayReconciliationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentGateway gateway;

    @Column(name = "run_at", nullable = false)
    @Builder.Default
    private LocalDateTime runAt = LocalDateTime.now();

    @Column(name = "total_checked", nullable = false)
    @Builder.Default
    private Integer totalChecked = 0;

    @Column(name = "discrepancies", nullable = false)
    @Builder.Default
    private Integer discrepancies = 0;

    @Column(name = "flagged_order_ids", columnDefinition = "JSONB")
    private String flaggedOrderIds;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(columnDefinition = "JSONB")
    private String details;
}
