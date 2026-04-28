package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.MilestoneStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_milestones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobMilestone extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private JobContract contract;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "milestone_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal milestoneAmount;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MilestoneStatus status = MilestoneStatus.PENDING;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "deliverables", columnDefinition = "TEXT")
    private String deliverables;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "client_feedback", columnDefinition = "TEXT")
    private String clientFeedback;

    @Column(name = "mentor_notes", columnDefinition = "TEXT")
    private String mentorNotes;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments; // JSON array of file URLs
}