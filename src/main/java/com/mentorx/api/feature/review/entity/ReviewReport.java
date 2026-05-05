package com.mentorx.api.feature.review.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing reports of inappropriate or fraudulent reviews
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "review_reports", indexes = {
    @Index(name = "idx_review_report_review_id", columnList = "review_id"),
    @Index(name = "idx_review_report_reporter_id", columnList = "reporter_id"),
    @Index(name = "idx_review_report_status", columnList = "status"),
    @Index(name = "idx_review_report_reason", columnList = "report_reason"),
    @Index(name = "idx_review_report_created", columnList = "created_at DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReport extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /**
     * Reason for reporting
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "report_reason", nullable = false, length = 50)
    private String reportReason; // SPAM, FAKE, OFFENSIVE, INAPPROPRIATE, MISLEADING, OTHER

    /**
     * Detailed description of the report
     */
    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Status of the report
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, UNDER_REVIEW, RESOLVED, DISMISSED

    /**
     * When the report was reviewed
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * ID of admin who reviewed this report
     */
    @Column(name = "reviewed_by_admin_id")
    private java.util.UUID reviewedByAdminId;

    /**
     * Action taken on the report
     */
    @Size(max = 50)
    @Column(name = "action_taken", length = 50)
    private String actionTaken; // REVIEW_HIDDEN, REVIEW_REMOVED, WARNING_ISSUED, USER_BANNED, NO_ACTION

    /**
     * Notes from the reviewer/admin
     */
    @Size(max = 500)
    @Column(name = "review_notes", length = 500)
    private String reviewNotes;

    /**
     * When the report was resolved
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * Whether the report was upheld
     */
    @Column(name = "is_upheld")
    private Boolean isUpheld;

    /**
     * Priority level of the report
     */
    @Column(name = "priority_level", columnDefinition = "INTEGER DEFAULT 1")
    private Integer priorityLevel = 1;

    /**
     * Whether this is a duplicate report
     */
    @Column(name = "is_duplicate", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDuplicate = false;

    /**
     * ID of the original report if this is a duplicate
     */
    @Column(name = "original_report_id")
    private java.util.UUID originalReportId;

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
     * Checks if the report is pending
     */
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    /**
     * Checks if the report is resolved
     */
    public boolean isResolved() {
        return "RESOLVED".equals(this.status) || "DISMISSED".equals(this.status);
    }

    /**
     * Marks the report as resolved
     */
    public void resolve(java.util.UUID adminId, String action, String notes, boolean upheld) {
        this.status = "RESOLVED";
        this.resolvedAt = LocalDateTime.now();
        this.reviewedAt = LocalDateTime.now();
        this.reviewedByAdminId = adminId;
        this.actionTaken = action;
        this.reviewNotes = notes;
        this.isUpheld = upheld;
    }

    /**
     * Marks the report as dismissed
     */
    public void dismiss(java.util.UUID adminId, String notes) {
        this.status = "DISMISSED";
        this.resolvedAt = LocalDateTime.now();
        this.reviewedAt = LocalDateTime.now();
        this.reviewedByAdminId = adminId;
        this.reviewNotes = notes;
        this.isUpheld = false;
    }
}