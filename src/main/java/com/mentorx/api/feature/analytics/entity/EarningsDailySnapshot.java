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

    @Column(name = "earned_from_mentoring_mxc", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal earnedFromMentoringMxc = BigDecimal.ZERO;

    @Column(name = "earned_from_freelance_mxc", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal earnedFromFreelanceMxc = BigDecimal.ZERO;

    @Column(name = "earned_from_courses_mxc", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal earnedFromCoursesMxc = BigDecimal.ZERO;

    @Column(name = "withdrawn_mxc", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal withdrawnMxc = BigDecimal.ZERO;

    @Column(name = "platform_fee_mxc", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal platformFeeMxc = BigDecimal.ZERO;

    @Column(name = "escrow_balance_mxc", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal escrowBalanceMxc = BigDecimal.ZERO;

    @Column(name = "available_balance_mxc", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal availableBalanceMxc = BigDecimal.ZERO;

    @Column(name = "jobs_completed", nullable = false)
    @Builder.Default
    private Integer jobsCompleted = 0;

    @Column(name = "contracts_completed", nullable = false)
    @Builder.Default
    private Integer contractsCompleted = 0;

    @Column(name = "contracts_active", nullable = false)
    @Builder.Default
    private Integer contractsActive = 0;

    @Column(name = "proposals_sent", nullable = false)
    @Builder.Default
    private Integer proposalsSent = 0;

    @Column(name = "proposals_accepted", nullable = false)
    @Builder.Default
    private Integer proposalsAccepted = 0;

    @Column(name = "courses_sold", nullable = false)
    @Builder.Default
    private Integer coursesSold = 0;

    @Column(name = "course_enrollments", nullable = false)
    @Builder.Default
    private Integer courseEnrollments = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
