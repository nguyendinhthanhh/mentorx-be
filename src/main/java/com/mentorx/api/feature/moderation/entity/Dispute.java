package com.mentorx.api.feature.moderation.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.moderation.enums.DisputeStatus;
import com.mentorx.api.feature.moderation.enums.DisputeOutcome;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Entity representing disputes between users (typically contract-related)
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "disputes", indexes = {
    @Index(name = "idx_dispute_initiator_id", columnList = "initiator_id"),
    @Index(name = "idx_dispute_respondent_id", columnList = "respondent_id"),
    @Index(name = "idx_dispute_contract_id", columnList = "contract_id"),
    @Index(name = "idx_dispute_status", columnList = "status"),
    @Index(name = "idx_dispute_mediator_id", columnList = "mediator_id"),
    @Index(name = "idx_dispute_created", columnList = "created_at DESC"),
    @Index(name = "idx_dispute_priority", columnList = "priority_level DESC, created_at ASC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Dispute extends BaseEntity {

    /**
     * User who initiated the dispute
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    /**
     * User responding to the dispute
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_id", nullable = false)
    private User respondent;

    /**
     * Contract this dispute is related to
     */
    @Column(name = "contract_id")
    private Long contractId;

    /**
     * Job this dispute is related to
     */
    @Column(name = "job_id")
    private Long jobId;

    /**
     * Title of the dispute
     */
    @NotNull
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Detailed description of the dispute
     */
    @NotNull
    @Size(max = 5000)
    @Column(name = "description", nullable = false, length = 5000)
    private String description;

    /**
     * Category of the dispute
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "dispute_category", nullable = false, length = 50)
    private String disputeCategory; // PAYMENT, QUALITY, DEADLINE, SCOPE, COMMUNICATION, CANCELLATION, REFUND, OTHER

    /**
     * Current status of the dispute
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DisputeStatus status = DisputeStatus.OPEN;

    /**
     * Priority level (1 = highest, 5 = lowest)
     */
    @Column(name = "priority_level", nullable = false, columnDefinition = "INTEGER DEFAULT 3")
    private Integer priorityLevel = 3;

    /**
     * Amount in dispute (in MXC)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "disputed_amount_mxc", precision = 10, scale = 2)
    private BigDecimal disputedAmountMxc;

    /**
     * Amount requested as refund (in MXC)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "refund_requested_mxc", precision = 10, scale = 2)
    private BigDecimal refundRequestedMxc;

    /**
     * Mediator assigned to this dispute
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mediator_id")
    private User mediator;

    /**
     * When the mediator was assigned
     */
    @Column(name = "mediator_assigned_at")
    private LocalDateTime mediatorAssignedAt;

    /**
     * When the respondent was notified
     */
    @Column(name = "respondent_notified_at")
    private LocalDateTime respondentNotifiedAt;

    /**
     * When the respondent responded
     */
    @Column(name = "respondent_responded_at")
    private LocalDateTime respondentRespondedAt;

    /**
     * Response from the respondent
     */
    @Size(max = 5000)
    @Column(name = "respondent_response", length = 5000)
    private String respondentResponse;

    /**
     * Deadline for respondent to respond
     */
    @Column(name = "response_deadline")
    private LocalDateTime responseDeadline;

    /**
     * When mediation started
     */
    @Column(name = "mediation_started_at")
    private LocalDateTime mediationStartedAt;

    /**
     * When the dispute was resolved
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * Outcome of the dispute
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 30)
    private DisputeOutcome outcome;

    /**
     * Final resolution details
     */
    @Size(max = 2000)
    @Column(name = "resolution_details", length = 2000)
    private String resolutionDetails;

    /**
     * Amount refunded (in MXC)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "refund_amount_mxc", precision = 10, scale = 2)
    private BigDecimal refundAmountMxc;

    /**
     * Whether funds are held in escrow
     */
    @Column(name = "funds_in_escrow", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean fundsInEscrow = false;

    /**
     * Escrow record ID
     */
    @Column(name = "escrow_record_id")
    private Long escrowRecordId;

    /**
     * Evidence items for this dispute
     */
    @OneToMany(mappedBy = "dispute", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DisputeEvidence> evidenceItems = new ArrayList<>();

    /**
     * Number of evidence items submitted by initiator
     */
    @Column(name = "initiator_evidence_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer initiatorEvidenceCount = 0;

    /**
     * Number of evidence items submitted by respondent
     */
    @Column(name = "respondent_evidence_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer respondentEvidenceCount = 0;

    /**
     * Additional metadata
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Internal notes from mediators
     */
    @Size(max = 5000)
    @Column(name = "internal_notes", length = 5000)
    private String internalNotes;

    /**
     * Whether this dispute requires arbitration
     */
    @Column(name = "requires_arbitration", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean requiresArbitration = false;

    /**
     * When arbitration started
     */
    @Column(name = "arbitration_started_at")
    private LocalDateTime arbitrationStartedAt;

    /**
     * Arbitrator assigned
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arbitrator_id")
    private User arbitrator;

    /**
     * Whether the initiator is satisfied with the outcome
     */
    @Column(name = "initiator_satisfied")
    private Boolean initiatorSatisfied;

    /**
     * Whether the respondent is satisfied with the outcome
     */
    @Column(name = "respondent_satisfied")
    private Boolean respondentSatisfied;

    /**
     * Time taken to resolve in hours
     */
    @Column(name = "resolution_time_hours", precision = 8, scale = 2)
    private BigDecimal resolutionTimeHours;

    /**
     * SLA deadline for resolution
     */
    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    /**
     * Whether SLA was met
     */
    @Column(name = "sla_met")
    private Boolean slaMet;

    @PrePersist
    protected void onCreate() {
        // Set response deadline (typically 3 days)
        if (this.responseDeadline == null) {
            this.responseDeadline = LocalDateTime.now().plusDays(3);
        }
        
        // Set SLA deadline based on priority
        if (this.slaDeadline == null && this.priorityLevel != null) {
            LocalDateTime now = LocalDateTime.now();
            switch (this.priorityLevel) {
                case 1: // Critical - 24 hours
                    this.slaDeadline = now.plusHours(24);
                    break;
                case 2: // High - 3 days
                    this.slaDeadline = now.plusDays(3);
                    break;
                case 3: // Medium - 7 days
                    this.slaDeadline = now.plusDays(7);
                    break;
                default: // Low - 14 days
                    this.slaDeadline = now.plusDays(14);
            }
        }
    }

    /**
     * Checks if the dispute is open
     */
    public boolean isOpen() {
        return DisputeStatus.OPEN.equals(this.status) || 
               DisputeStatus.INVESTIGATING.equals(this.status) ||
               DisputeStatus.AWAITING_RESPONSE.equals(this.status) ||
               DisputeStatus.EVIDENCE_REVIEW.equals(this.status) ||
               DisputeStatus.IN_MEDIATION.equals(this.status) ||
               DisputeStatus.IN_ARBITRATION.equals(this.status);
    }

    /**
     * Checks if the dispute is resolved
     */
    public boolean isResolved() {
        return DisputeStatus.RESOLVED.equals(this.status) ||
               DisputeStatus.CLOSED.equals(this.status) ||
               DisputeStatus.WITHDRAWN.equals(this.status);
    }

    /**
     * Checks if response deadline has passed
     */
    public boolean isResponseOverdue() {
        return this.responseDeadline != null && 
               LocalDateTime.now().isAfter(this.responseDeadline) &&
               this.respondentRespondedAt == null;
    }

    /**
     * Assigns a mediator to the dispute
     */
    public void assignMediator(User mediator) {
        this.mediator = mediator;
        this.mediatorAssignedAt = LocalDateTime.now();
        this.status = DisputeStatus.IN_MEDIATION;
        this.mediationStartedAt = LocalDateTime.now();
    }

    /**
     * Resolves the dispute
     */
    public void resolve(DisputeOutcome outcome, String details, BigDecimal refundAmount) {
        this.status = DisputeStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.outcome = outcome;
        this.resolutionDetails = details;
        this.refundAmountMxc = refundAmount;
        
        // Calculate resolution time
        if (this.getCreatedAt() != null) {
            long hours = java.time.Duration.between(this.getCreatedAt(), this.resolvedAt).toHours();
            this.resolutionTimeHours = BigDecimal.valueOf(hours);
        }
        
        // Check if SLA was met
        this.slaMet = this.slaDeadline == null || this.resolvedAt.isBefore(this.slaDeadline);
    }

    /**
     * Escalates to arbitration
     */
    public void escalateToArbitration(User arbitrator) {
        this.requiresArbitration = true;
        this.arbitrator = arbitrator;
        this.arbitrationStartedAt = LocalDateTime.now();
        this.status = DisputeStatus.IN_ARBITRATION;
    }

    /**
     * Withdraws the dispute
     */
    public void withdraw() {
        this.status = DisputeStatus.WITHDRAWN;
        this.resolvedAt = LocalDateTime.now();
    }
}