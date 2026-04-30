package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.job.enums.ContractStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Entity representing contracts between clients and mentors
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "contracts", indexes = {
    @Index(name = "idx_contract_job_id", columnList = "job_id"),
    @Index(name = "idx_contract_client_id", columnList = "client_id"),
    @Index(name = "idx_contract_mentor_id", columnList = "mentor_id"),
    @Index(name = "idx_contract_proposal_id", columnList = "proposal_id"),
    @Index(name = "idx_contract_status", columnList = "status"),
    @Index(name = "idx_contract_created", columnList = "created_at DESC"),
    @Index(name = "idx_contract_start_date", columnList = "start_date"),
    @Index(name = "idx_contract_end_date", columnList = "end_date")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Contract extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id")
    private Proposal proposal;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    /**
     * Contract status
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ContractStatus status = ContractStatus.DRAFT;

    /**
     * Contract title
     */
    @NotNull
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Contract description/scope of work
     */
    @NotNull
    @Size(max = 5000)
    @Column(name = "description", nullable = false, length = 5000)
    private String description;

    /**
     * Total contract amount (in MXC)
     */
    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    /**
     * Hourly rate (if applicable)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "hourly_rate", precision = 8, scale = 2)
    private BigDecimal hourlyRate;

    /**
     * Platform fee percentage
     */
    @DecimalMin(value = "0.0")
    @Column(name = "platform_fee_percentage", precision = 5, scale = 2)
    private BigDecimal platformFeePercentage;

    /**
     * Platform fee amount (in MXC)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "platform_fee_amount", precision = 10, scale = 2)
    private BigDecimal platformFeeAmount;

    /**
     * Mentor net amount after fees
     */
    @DecimalMin(value = "0.0")
    @Column(name = "mentor_net_amount", precision = 10, scale = 2)
    private BigDecimal mentorNetAmount;

    /**
     * Contract start date
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * Contract end date
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Actual start date
     */
    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    /**
     * Actual completion date
     */
    @Column(name = "actual_completion_date")
    private LocalDate actualCompletionDate;

    /**
     * Terms and conditions
     */
    @Size(max = 5000)
    @Column(name = "terms_and_conditions", length = 5000)
    private String termsAndConditions;

    /**
     * Payment terms
     */
    @Size(max = 1000)
    @Column(name = "payment_terms", length = 1000)
    private String paymentTerms;

    /**
     * Deliverables description
     */
    @Size(max = 2000)
    @Column(name = "deliverables", length = 2000)
    private String deliverables;

    /**
     * Client signature
     */
    @Size(max = 500)
    @Column(name = "client_signature", length = 500)
    private String clientSignature;

    /**
     * When client signed
     */
    @Column(name = "client_signed_at")
    private LocalDateTime clientSignedAt;

    /**
     * Client IP address at signing
     */
    @Size(max = 45)
    @Column(name = "client_sign_ip", length = 45)
    private String clientSignIp;

    /**
     * Mentor signature
     */
    @Size(max = 500)
    @Column(name = "mentor_signature", length = 500)
    private String mentorSignature;

    /**
     * When mentor signed
     */
    @Column(name = "mentor_signed_at")
    private LocalDateTime mentorSignedAt;

    /**
     * Mentor IP address at signing
     */
    @Size(max = 45)
    @Column(name = "mentor_sign_ip", length = 45)
    private String mentorSignIp;

    /**
     * When contract became active
     */
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    /**
     * When contract was completed
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * When contract was cancelled
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /**
     * Cancellation reason
     */
    @Size(max = 500)
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    /**
     * Who cancelled the contract
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by_user_id")
    private User cancelledByUser;

    /**
     * Milestones for this contract
     */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Milestone> milestones = new ArrayList<>();

    /**
     * Number of milestones
     */
    @Column(name = "milestone_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer milestoneCount = 0;

    /**
     * Number of completed milestones
     */
    @Column(name = "completed_milestone_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer completedMilestoneCount = 0;

    /**
     * Total amount paid so far
     */
    @DecimalMin(value = "0.0")
    @Column(name = "amount_paid", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal amountPaid = BigDecimal.ZERO;

    /**
     * Amount in escrow
     */
    @DecimalMin(value = "0.0")
    @Column(name = "amount_in_escrow", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal amountInEscrow = BigDecimal.ZERO;

    /**
     * Whether funds are in escrow
     */
    @Column(name = "funds_in_escrow", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean fundsInEscrow = false;

    /**
     * Escrow record ID
     */
    @Column(name = "escrow_record_id")
    private Long escrowRecordId;

    /**
     * Progress percentage (0-100)
     */
    @Column(name = "progress_percentage", columnDefinition = "INTEGER DEFAULT 0")
    private Integer progressPercentage = 0;

    /**
     * Additional metadata
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Whether contract is renewable
     */
    @Column(name = "is_renewable", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isRenewable = false;

    /**
     * Auto-renewal enabled
     */
    @Column(name = "auto_renewal", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean autoRenewal = false;

    /**
     * Renewal terms
     */
    @Size(max = 1000)
    @Column(name = "renewal_terms", length = 1000)
    private String renewalTerms;

    /**
     * NDA required
     */
    @Column(name = "nda_required", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean ndaRequired = false;

    /**
     * NDA signed
     */
    @Column(name = "nda_signed", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean ndaSigned = false;

    /**
     * Checks if both parties have signed
     */
    public boolean isFullySigned() {
        return this.clientSignedAt != null && this.mentorSignedAt != null;
    }

    /**
     * Activates the contract
     */
    public void activate() {
        if (!isFullySigned()) {
            throw new IllegalStateException("Contract must be fully signed before activation");
        }
        this.status = ContractStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
        if (this.actualStartDate == null) {
            this.actualStartDate = LocalDate.now();
        }
    }

    /**
     * Completes the contract
     */
    public void complete() {
        this.status = ContractStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.actualCompletionDate = LocalDate.now();
        this.progressPercentage = 100;
    }

    /**
     * Cancels the contract
     */
    public void cancel(User cancelledBy, String reason) {
        this.status = ContractStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelledByUser = cancelledBy;
        this.cancellationReason = reason;
    }

    /**
     * Updates progress percentage
     */
    public void updateProgress() {
        if (this.milestoneCount > 0) {
            this.progressPercentage = (this.completedMilestoneCount * 100) / this.milestoneCount;
        }
    }

    /**
     * Checks if contract is active
     */
    public boolean isActive() {
        return ContractStatus.ACTIVE.equals(this.status);
    }

    /**
     * Checks if contract is overdue
     */
    public boolean isOverdue() {
        return this.endDate != null && 
               LocalDate.now().isAfter(this.endDate) && 
               !ContractStatus.COMPLETED.equals(this.status);
    }
}