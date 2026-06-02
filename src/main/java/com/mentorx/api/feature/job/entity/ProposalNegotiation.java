package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.job.enums.NegotiationStatus;
import com.mentorx.api.feature.job.enums.SenderType;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing negotiation rounds between client and mentor
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "proposal_negotiations",
       indexes = {
    @Index(name = "idx_negotiation_proposal_id", columnList = "proposal_id"),
    @Index(name = "idx_negotiation_sender_id", columnList = "sender_id"),
    @Index(name = "idx_negotiation_status", columnList = "status"),
    @Index(name = "idx_negotiation_created", columnList = "created_at DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProposalNegotiation extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Who sent this negotiation: CLIENT or MENTOR
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 20)
    private SenderType senderType;

    /**
     * Negotiation message
     */
    @NotNull
    @Size(max = 2000)
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Counter-proposed amount (in MXC)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "proposed_amount", precision = 10, scale = 2)
    private BigDecimal proposedAmount;

    /**
     * Counter-proposed hourly rate (if applicable)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "proposed_hourly_rate", precision = 8, scale = 2)
    private BigDecimal proposedHourlyRate;

    /**
     * Counter-proposed duration in days
     */
    @Column(name = "estimated_duration_days")
    private Integer estimatedDurationDays;

    /**
     * Counter-proposed start date
     */
    @Column(name = "proposed_start_date")
    private LocalDate proposedStartDate;

    /**
     * Counter-proposed delivery date
     */
    @Column(name = "proposed_delivery_date")
    private LocalDate proposedDeliveryDate;

    /**
     * Negotiation status
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NegotiationStatus status = NegotiationStatus.PENDING;

    /**
     * When this negotiation was responded to
     */
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    /**
     * Accepts this negotiation offer
     */
    public void accept() {
        this.status = NegotiationStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * Rejects this negotiation offer
     */
    public void reject() {
        this.status = NegotiationStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * Marks as countered (new counter-offer made)
     */
    public void counter() {
        this.status = NegotiationStatus.COUNTERED;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * Cancels this negotiation because the proposal/job is no longer actionable.
     */
    public void cancel() {
        this.status = NegotiationStatus.CANCELLED;
        this.respondedAt = LocalDateTime.now();
    }
}
