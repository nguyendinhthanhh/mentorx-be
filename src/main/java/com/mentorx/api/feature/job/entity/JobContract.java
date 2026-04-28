package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.ContractStatus;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "job_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobContract extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id")
    private JobProposal proposal;

    @Column(name = "contract_title", nullable = false, length = 200)
    private String contractTitle;

    @Column(name = "contract_description", columnDefinition = "TEXT")
    private String contractDescription;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContractStatus status = ContractStatus.ACTIVE;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "client_signed_at")
    private LocalDateTime clientSignedAt;

    @Column(name = "mentor_signed_at")
    private LocalDateTime mentorSignedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "escrow_amount", precision = 10, scale = 2)
    private BigDecimal escrowAmount;

    @Column(name = "escrow_released_at")
    private LocalDateTime escrowReleasedAt;

    // Relationships
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JobMilestone> milestones;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JobDeliverable> deliverables;
}