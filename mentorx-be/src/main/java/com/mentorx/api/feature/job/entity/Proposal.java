package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.job.enums.ProposalStatus;
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
 * Entity representing proposals submitted by mentors for jobs
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "proposals", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "mentor_id"}),
       indexes = {
    @Index(name = "idx_proposal_job_id", columnList = "job_id"),
    @Index(name = "idx_proposal_mentor_id", columnList = "mentor_id"),
    @Index(name = "idx_proposal_status", columnList = "status"),
    @Index(name = "idx_proposal_created", columnList = "created_at DESC"),
    @Index(name = "idx_proposal_amount", columnList = "proposed_amount")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Proposal extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    /**
     * Proposal status
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProposalStatus status = ProposalStatus.DRAFT;

    /**
     * Cover letter/pitch
     */
    @NotNull
    @Size(max = 5000)
    @Column(name = "cover_letter", nullable = false, length = 5000)
    private String coverLetter;

    /**
     * Proposed amount (in MXC)
     */
    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "proposed_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal proposedAmount;

    /**
     * Proposed hourly rate (if applicable)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "proposed_hourly_rate", precision = 8, scale = 2)
    private BigDecimal proposedHourlyRate;

    /**
     * Estimated duration in days
     */
    @Column(name = "estimated_duration_days")
    private Integer estimatedDurationDays;

    /**
     * Proposed start date
     */
    @Column(name = "proposed_start_date")
    private LocalDate proposedStartDate;

    /**
     * Proposed delivery date
     */
    @Column(name = "proposed_delivery_date")
    private LocalDate proposedDeliveryDate;

    /**
     * Milestones proposed (as JSON)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "proposed_milestones", columnDefinition = "jsonb")
    private List<Map<String, Object>> proposedMilestones = new ArrayList<>();

    /**
     * Relevant experience description
     */
    @Size(max = 2000)
    @Column(name = "relevant_experience", length = 2000)
    private String relevantExperience;

    /**
     * Portfolio items/samples
     */
    @ElementCollection
    @CollectionTable(name = "proposal_portfolio_links", 
                    joinColumns = @JoinColumn(name = "proposal_id"))
    @Column(name = "portfolio_url", length = 500)
    private List<String> portfolioLinks = new ArrayList<>();

    /**
     * Attachments (resumes, certificates, etc.)
     */
    @ElementCollection
    @CollectionTable(name = "proposal_attachments", 
                    joinColumns = @JoinColumn(name = "proposal_id"))
    @Column(name = "attachment_url", length = 500)
    private List<String> attachments = new ArrayList<>();

    /**
     * Questions for the client
     */
    @Size(max = 1000)
    @Column(name = "questions", length = 1000)
    private String questions;

    /**
     * Additional terms and conditions
     */
    @Size(max = 1000)
    @Column(name = "terms", length = 1000)
    private String terms;

    /**
     * When the proposal was submitted
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * When the proposal was viewed by client
     */
    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    /**
     * Number of times viewed by client
     */
    @Column(name = "view_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer viewCount = 0;

    /**
     * When the proposal was shortlisted
     */
    @Column(name = "shortlisted_at")
    private LocalDateTime shortlistedAt;

    /**
     * When the proposal was accepted
     */
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    /**
     * When the proposal was rejected
     */
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    /**
     * Rejection reason
     */
    @Size(max = 500)
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    /**
     * Client feedback on proposal
     */
    @Size(max = 1000)
    @Column(name = "client_feedback", length = 1000)
    private String clientFeedback;

    /**
     * Whether interview was requested
     */
    @Column(name = "interview_requested", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean interviewRequested = false;

    /**
     * Interview scheduled date/time
     */
    @Column(name = "interview_scheduled_at")
    private LocalDateTime interviewScheduledAt;

    /**
     * Whether proposal is featured/boosted
     */
    @Column(name = "is_featured", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isFeatured = false;

    /**
     * Featured until date
     */
    @Column(name = "featured_until")
    private LocalDateTime featuredUntil;

    /**
     * Proposal score/ranking
     */
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    /**
     * Additional metadata
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Whether this is a counter-proposal
     */
    @Column(name = "is_counter_proposal", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isCounterProposal = false;

    /**
     * Original proposal ID if this is a counter
     */
    @Column(name = "original_proposal_id")
    private Long originalProposalId;

    /**
     * Submits the proposal
     */
    public void submit() {
        this.status = ProposalStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    /**
     * Marks as viewed by client
     */
    public void markAsViewed() {
        if (this.viewedAt == null) {
            this.viewedAt = LocalDateTime.now();
        }
        this.viewCount++;
    }

    /**
     * Shortlists the proposal
     */
    public void shortlist() {
        this.status = ProposalStatus.SHORTLISTED;
        this.shortlistedAt = LocalDateTime.now();
    }

    /**
     * Accepts the proposal
     */
    public void accept() {
        this.status = ProposalStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    /**
     * Rejects the proposal
     */
    public void reject(String reason) {
        this.status = ProposalStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    /**
     * Withdraws the proposal
     */
    public void withdraw() {
        this.status = ProposalStatus.WITHDRAWN;
    }

    /**
     * Checks if proposal is active
     */
    public boolean isActive() {
        return ProposalStatus.SUBMITTED.equals(this.status) ||
               ProposalStatus.UNDER_REVIEW.equals(this.status) ||
               ProposalStatus.SHORTLISTED.equals(this.status) ||
               ProposalStatus.NEGOTIATING.equals(this.status);
    }

    /**
     * Checks if proposal is featured and not expired
     */
    public boolean isCurrentlyFeatured() {
        return this.isFeatured && 
               this.featuredUntil != null && 
               LocalDateTime.now().isBefore(this.featuredUntil);
    }
}