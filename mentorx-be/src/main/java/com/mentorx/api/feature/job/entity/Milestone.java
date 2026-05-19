package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.job.enums.MilestoneStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing milestones within a contract
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "milestones", indexes = {
    @Index(name = "idx_milestone_contract_id", columnList = "contract_id"),
    @Index(name = "idx_milestone_status", columnList = "status"),
    @Index(name = "idx_milestone_due_date", columnList = "due_date"),
    @Index(name = "idx_milestone_order", columnList = "milestone_order")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Milestone extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    /**
     * Milestone title
     */
    @NotNull
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Milestone description
     */
    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Milestone order/sequence
     */
    @NotNull
    @Min(value = 1)
    @Column(name = "milestone_order", nullable = false)
    private Integer milestoneOrder;

    /**
     * Milestone status
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MilestoneStatus status = MilestoneStatus.PENDING;

    /**
     * Milestone amount (in MXC)
     */
    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /**
     * Due date
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /**
     * When work started
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /**
     * When milestone was submitted
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * When milestone was approved
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * When milestone was completed and paid
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Deliverables for this milestone
     */
    @OneToMany(mappedBy = "milestone", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MilestoneDeliverable> deliverables = new ArrayList<>();

    /**
     * Submission notes from mentor
     */
    @Size(max = 1000)
    @Column(name = "submission_notes", length = 1000)
    private String submissionNotes;

    /**
     * Review notes from client
     */
    @Size(max = 1000)
    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    /**
     * Revision count
     */
    @Column(name = "revision_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer revisionCount = 0;

    /**
     * Maximum revisions allowed
     */
    @Column(name = "max_revisions", columnDefinition = "INTEGER DEFAULT 2")
    private Integer maxRevisions = 2;

    /**
     * Whether payment has been released
     */
    @Column(name = "payment_released", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean paymentReleased = false;

    /**
     * When payment was released
     */
    @Column(name = "payment_released_at")
    private LocalDateTime paymentReleasedAt;

    /**
     * Transaction ID for payment
     */
    @Column(name = "payment_transaction_id")
    private Long paymentTransactionId;

    /**
     * Whether this milestone is optional
     */
    @Column(name = "is_optional", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isOptional = false;

    /**
     * Whether this milestone depends on previous milestone
     */
    @Column(name = "depends_on_previous", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean dependsOnPrevious = true;

    /**
     * Starts the milestone
     */
    public void start() {
        this.status = MilestoneStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * Submits the milestone for review
     */
    public void submit(String notes) {
        this.status = MilestoneStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
        this.submissionNotes = notes;
    }

    /**
     * Approves the milestone
     */
    public void approve(String reviewNotes) {
        this.status = MilestoneStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        this.reviewNotes = reviewNotes;
    }

    /**
     * Requests revision
     */
    public void requestRevision(String reviewNotes) {
        if (this.revisionCount >= this.maxRevisions) {
            throw new IllegalStateException("Maximum revisions exceeded");
        }
        this.status = MilestoneStatus.REVISION_REQUESTED;
        this.reviewNotes = reviewNotes;
        this.revisionCount++;
    }

    /**
     * Completes the milestone with payment
     */
    public void complete(Long transactionId) {
        this.status = MilestoneStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.paymentReleased = true;
        this.paymentReleasedAt = LocalDateTime.now();
        this.paymentTransactionId = transactionId;
    }

    /**
     * Checks if milestone is overdue
     */
    public boolean isOverdue() {
        return this.dueDate != null && 
               LocalDate.now().isAfter(this.dueDate) &&
               !MilestoneStatus.COMPLETED.equals(this.status) &&
               !MilestoneStatus.APPROVED.equals(this.status);
    }

    /**
     * Checks if can request more revisions
     */
    public boolean canRequestRevision() {
        return this.revisionCount < this.maxRevisions;
    }
}