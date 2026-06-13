package com.mentorx.api.feature.complaint.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.complaint.enums.ComplaintOutcome;
import com.mentorx.api.feature.complaint.enums.ComplaintStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "disputes", indexes = {
    @Index(name = "idx_dispute_complainant_id", columnList = "complainant_id"),
    @Index(name = "idx_dispute_respondent_id", columnList = "respondent_id"),
    @Index(name = "idx_dispute_status", columnList = "status"),
    @Index(name = "idx_dispute_booking_id", columnList = "booking_id"),
    @Index(name = "idx_dispute_created", columnList = "created_at DESC")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Complaint extends BaseEntity {

    @Column(name = "complainant_id", nullable = false)
    private UUID complainantId;

    @Column(name = "respondent_id", nullable = false)
    private UUID respondentId;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "booking_id")
    private UUID bookingId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "complaint_category", nullable = false, length = 50)
    private String complaintCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.OPEN;

    @Column(name = "priority_level", nullable = false)
    @Builder.Default
    private Integer priorityLevel = 3;

    @Column(name = "mediator_id")
    private UUID mediatorId;

    @Column(name = "mediator_assigned_at")
    private LocalDateTime mediatorAssignedAt;

    @Column(name = "respondent_notified_at")
    private LocalDateTime respondentNotifiedAt;

    @Column(name = "respondent_responded_at")
    private LocalDateTime respondentRespondedAt;

    @Column(name = "respondent_response", columnDefinition = "TEXT")
    private String respondentResponse;

    @Column(name = "response_deadline")
    private LocalDateTime responseDeadline;

    @Column(name = "mediation_started_at")
    private LocalDateTime mediationStartedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 30)
    private ComplaintOutcome outcome;

    @Column(name = "resolution_details", length = 2000)
    private String resolutionDetails;

    @Column(name = "resolution_time_hours")
    private Double resolutionTimeHours;

    @Column(name = "sla_met")
    private Boolean slaMet;

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @PrePersist
    protected void onCreate() {
        if (this.responseDeadline == null) {
            this.responseDeadline = LocalDateTime.now().plusDays(3);
        }
        if (this.slaDeadline == null && this.priorityLevel != null) {
            LocalDateTime now = LocalDateTime.now();
            switch (this.priorityLevel) {
                case 1:
                    this.slaDeadline = now.plusHours(24);
                    break;
                case 2:
                    this.slaDeadline = now.plusDays(3);
                    break;
                case 3:
                    this.slaDeadline = now.plusDays(7);
                    break;
                default:
                    this.slaDeadline = now.plusDays(14);
            }
        }
    }

    public void transitionStatus(ComplaintStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new AppException(ErrorCode.INVALID_DISPUTE_STATUS,
                "Cannot transition from " + this.status + " to " + newStatus);
        }
        this.status = newStatus;
    }

    public void assignMediator(UUID mediatorId) {
        transitionStatus(ComplaintStatus.IN_MEDIATION);
        this.mediatorId = mediatorId;
        this.mediatorAssignedAt = LocalDateTime.now();
        this.mediationStartedAt = LocalDateTime.now();
    }

    public void resolve(ComplaintOutcome outcome, String details) {
        transitionStatus(ComplaintStatus.RESOLVED);
        this.outcome = outcome;
        this.resolutionDetails = details;
        this.resolvedAt = LocalDateTime.now();
        if (getCreatedAt() != null) {
            this.resolutionTimeHours = (double) Duration.between(getCreatedAt(), this.resolvedAt).toHours();
        }
        this.slaMet = this.slaDeadline == null || this.resolvedAt.isBefore(this.slaDeadline);
    }

    public void withdraw() {
        transitionStatus(ComplaintStatus.WITHDRAWN);
    }

    public void expire() {
        transitionStatus(ComplaintStatus.EXPIRED);
    }
}
