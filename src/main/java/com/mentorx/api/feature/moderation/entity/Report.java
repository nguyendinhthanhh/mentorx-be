package com.mentorx.api.feature.moderation.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.moderation.enums.ReportStatus;
import com.mentorx.api.feature.moderation.enums.ReportTargetType;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity representing content reports for moderation
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "reports", indexes = {
    @Index(name = "idx_report_reporter_id", columnList = "reporter_id"),
    @Index(name = "idx_report_target", columnList = "target_type, target_id"),
    @Index(name = "idx_report_status", columnList = "status"),
    @Index(name = "idx_report_priority", columnList = "priority_level DESC, created_at ASC"),
    @Index(name = "idx_report_assigned", columnList = "assigned_to_admin_id"),
    @Index(name = "idx_report_created", columnList = "created_at DESC"),
    @Index(name = "idx_report_category", columnList = "report_category")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /**
     * Type of entity being reported
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private ReportTargetType targetType;

    /**
     * ID of the entity being reported
     */
    @NotNull
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /**
     * User who owns/created the reported content (if applicable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    /**
     * Category of the report
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "report_category", nullable = false, length = 50)
    private String reportCategory; // SPAM, HARASSMENT, INAPPROPRIATE_CONTENT, FRAUD, COPYRIGHT, VIOLENCE, HATE_SPEECH, MISINFORMATION, OTHER

    /**
     * Detailed reason for the report
     */
    @NotNull
    @Size(max = 2000)
    @Column(name = "reason", nullable = false, length = 2000)
    private String reason;

    /**
     * Current status of the report
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    /**
     * Priority level (1 = highest, 5 = lowest)
     */
    @Min(value = 1)
    @Column(name = "priority_level", nullable = false, columnDefinition = "INTEGER DEFAULT 3")
    private Integer priorityLevel = 3;

    /**
     * Admin assigned to handle this report
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_admin_id")
    private User assignedToAdmin;

    /**
     * When the report was assigned
     */
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    /**
     * When the report was first reviewed
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * When the report was resolved
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * Action taken on the report
     */
    @Size(max = 50)
    @Column(name = "action_taken", length = 50)
    private String actionTaken; // CONTENT_REMOVED, USER_WARNED, USER_SUSPENDED, USER_BANNED, NO_ACTION, CONTENT_EDITED, OTHER

    /**
     * Notes from the moderator
     */
    @Size(max = 2000)
    @Column(name = "moderator_notes", length = 2000)
    private String moderatorNotes;

    /**
     * Whether the report was upheld
     */
    @Column(name = "is_upheld")
    private Boolean isUpheld;

    /**
     * Whether this is a duplicate report
     */
    @Column(name = "is_duplicate", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDuplicate = false;

    /**
     * ID of the original report if this is a duplicate
     */
    @Column(name = "original_report_id")
    private Long originalReportId;

    /**
     * Number of similar reports for the same target
     */
    @Min(value = 0)
    @Column(name = "similar_report_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer similarReportCount = 0;

    /**
     * Whether this report requires urgent attention
     */
    @Column(name = "is_urgent", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isUrgent = false;

    /**
     * Whether the reported content has been temporarily hidden
     */
    @Column(name = "content_hidden", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean contentHidden = false;

    /**
     * When the content was hidden
     */
    @Column(name = "content_hidden_at")
    private LocalDateTime contentHiddenAt;

    /**
     * Evidence URLs (screenshots, links, etc.)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence_urls", columnDefinition = "jsonb")
    private Map<String, Object> evidenceUrls;

    /**
     * Additional metadata
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * IP address of the reporter
     */
    @Size(max = 45)
    @Column(name = "reporter_ip", length = 45)
    private String reporterIp;

    /**
     * User agent of the reporter
     */
    @Size(max = 500)
    @Column(name = "reporter_user_agent", length = 500)
    private String reporterUserAgent;

    /**
     * Context where the report was made (page URL, etc.)
     */
    @Size(max = 500)
    @Column(name = "report_context", length = 500)
    private String reportContext;

    /**
     * Escalation level (0 = not escalated, higher = more escalated)
     */
    @Min(value = 0)
    @Column(name = "escalation_level", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer escalationLevel = 0;

    /**
     * When the report was last escalated
     */
    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    /**
     * Reason for escalation
     */
    @Size(max = 500)
    @Column(name = "escalation_reason", length = 500)
    private String escalationReason;

    /**
     * SLA deadline for this report
     */
    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    /**
     * Whether SLA was met
     */
    @Column(name = "sla_met")
    private Boolean slaMet;

    /**
     * Time taken to resolve in hours
     */
    @Column(name = "resolution_time_hours", precision = 8, scale = 2)
    private java.math.BigDecimal resolutionTimeHours;

    @PrePersist
    protected void onCreate() {
        // Set SLA deadline based on priority
        if (this.slaDeadline == null && this.priorityLevel != null) {
            LocalDateTime now = LocalDateTime.now();
            switch (this.priorityLevel) {
                case 1: // Critical - 2 hours
                    this.slaDeadline = now.plusHours(2);
                    break;
                case 2: // High - 8 hours
                    this.slaDeadline = now.plusHours(8);
                    break;
                case 3: // Medium - 24 hours
                    this.slaDeadline = now.plusHours(24);
                    break;
                case 4: // Low - 72 hours
                    this.slaDeadline = now.plusHours(72);
                    break;
                default: // Very Low - 7 days
                    this.slaDeadline = now.plusDays(7);
            }
        }
    }

    /**
     * Checks if the report is pending
     */
    public boolean isPending() {
        return ReportStatus.PENDING.equals(this.status);
    }

    /**
     * Checks if the report is resolved
     */
    public boolean isResolved() {
        return ReportStatus.RESOLVED.equals(this.status) || 
               ReportStatus.DISMISSED.equals(this.status) ||
               ReportStatus.CLOSED.equals(this.status);
    }

    /**
     * Checks if SLA deadline has passed
     */
    public boolean isSlaBreached() {
        return this.slaDeadline != null && 
               LocalDateTime.now().isAfter(this.slaDeadline) && 
               !isResolved();
    }

    /**
     * Assigns the report to an admin
     */
    public void assignTo(User admin) {
        this.assignedToAdmin = admin;
        this.assignedAt = LocalDateTime.now();
        if (this.status == ReportStatus.PENDING) {
            this.status = ReportStatus.UNDER_REVIEW;
        }
    }

    /**
     * Escalates the report
     */
    public void escalate(String reason) {
        this.escalationLevel++;
        this.escalatedAt = LocalDateTime.now();
        this.escalationReason = reason;
        this.status = ReportStatus.ESCALATED;
    }

    /**
     * Resolves the report
     */
    public void resolve(String action, String notes, boolean upheld) {
        this.status = ReportStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.actionTaken = action;
        this.moderatorNotes = notes;
        this.isUpheld = upheld;
        
        // Calculate resolution time
        if (this.getCreatedAt() != null) {
            long hours = java.time.Duration.between(this.getCreatedAt(), this.resolvedAt).toHours();
            this.resolutionTimeHours = java.math.BigDecimal.valueOf(hours);
        }
        
        // Check if SLA was met
        this.slaMet = this.slaDeadline == null || this.resolvedAt.isBefore(this.slaDeadline);
    }

    /**
     * Hides the reported content temporarily
     */
    public void hideContent() {
        this.contentHidden = true;
        this.contentHiddenAt = LocalDateTime.now();
    }
}