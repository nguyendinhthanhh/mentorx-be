package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.ProposalStatus;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_proposals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobProposal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "proposed_rate", precision = 10, scale = 2)
    private BigDecimal proposedRate;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "estimated_duration_days")
    private Integer estimatedDurationDays;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProposalStatus status = ProposalStatus.PENDING;

    @Column(name = "client_feedback", columnDefinition = "TEXT")
    private String clientFeedback;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments; // JSON array of file URLs

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
}