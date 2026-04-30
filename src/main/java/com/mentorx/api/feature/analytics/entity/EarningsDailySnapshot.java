package com.mentorx.api.feature.analytics.entity;

import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "earnings_daily_snapshots", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "snapshot_date"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EarningsDailySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "earned_mxc", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal earnedMxc = BigDecimal.ZERO;

    @Column(name = "withdrawn_mxc", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal withdrawnMxc = BigDecimal.ZERO;

    @Column(name = "platform_fee_mxc", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal platformFeeMxc = BigDecimal.ZERO;

    @Column(name = "jobs_completed", nullable = false)
    @Builder.Default
    private Short jobsCompleted = 0;

    @Column(name = "courses_sold", nullable = false)
    @Builder.Default
    private Short coursesSold = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
