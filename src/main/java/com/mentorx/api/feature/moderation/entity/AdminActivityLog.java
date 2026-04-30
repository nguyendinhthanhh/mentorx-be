package com.mentorx.api.feature.moderation.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
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
 * Entity representing admin activity logs for audit trail
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "admin_activity_logs", indexes = {
    @Index(name = "idx_admin_log_admin_id", columnList = "admin_id"),
    @Index(name = "idx_admin_log_action", columnList = "action_type"),
    @Index(name = "idx_admin_log_target", columnList = "target_type, target_id"),
    @Index(name = "idx_admin_log_timestamp", columnList = "action_timestamp DESC"),
    @Index(name = "idx_admin_log_severity", columnList = "severity_level")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AdminActivityLog extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    /**
     * Type of action performed
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // USER_BANNED, USER_WARNED, CONTENT_REMOVED, REPORT_RESOLVED, DISPUTE_RESOLVED, SETTINGS_CHANGED, etc.

    /**
     * Category of the action
     */
    @Size(max = 30)
    @Column(name = "action_category", length = 30)
    private String actionCategory; // MODERATION, USER_MANAGEMENT, CONTENT_MANAGEMENT, SYSTEM_CONFIG, FINANCIAL, etc.

    /**
     * Description of the action
     */
    @NotNull
    @Size(max = 1000)
    @Column(name = "action_description", nullable = false, length = 1000)
    private String actionDescription;

    /**
     * Type of entity affected
     */
    @Size(max = 50)
    @Column(name = "target_type", length = 50)
    private String targetType;

    /**
     * ID of the entity affected
     */
    @Column(name = "target_id")
    private Long targetId;

    /**
     * User affected by the action (if applicable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affected_user_id")
    private User affectedUser;

    /**
     * When the action was performed
     */
    @NotNull
    @Column(name = "action_timestamp", nullable = false)
    private LocalDateTime actionTimestamp;

    /**
     * Severity level of the action
     */
    @Size(max = 20)
    @Column(name = "severity_level", length = 20)
    private String severityLevel; // INFO, WARNING, CRITICAL

    /**
     * Previous state/value before the action
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "previous_state", columnDefinition = "jsonb")
    private Map<String, Object> previousState;

    /**
     * New state/value after the action
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_state", columnDefinition = "jsonb")
    private Map<String, Object> newState;

    /**
     * Additional metadata
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * IP address of the admin
     */
    @Size(max = 45)
    @Column(name = "admin_ip", length = 45)
    private String adminIp;

    /**
     * User agent of the admin
     */
    @Size(max = 500)
    @Column(name = "admin_user_agent", length = 500)
    private String adminUserAgent;

    /**
     * Session ID
     */
    @Size(max = 100)
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * Whether this action was successful
     */
    @Column(name = "is_successful", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isSuccessful = true;

    /**
     * Error message if action failed
     */
    @Size(max = 500)
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /**
     * Reason/justification for the action
     */
    @Size(max = 1000)
    @Column(name = "reason", length = 1000)
    private String reason;

    /**
     * Related report ID (if applicable)
     */
    @Column(name = "related_report_id")
    private Long relatedReportId;

    /**
     * Related dispute ID (if applicable)
     */
    @Column(name = "related_dispute_id")
    private Long relatedDisputeId;

    /**
     * Whether this action requires approval
     */
    @Column(name = "requires_approval", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean requiresApproval = false;

    /**
     * Whether this action has been approved
     */
    @Column(name = "is_approved")
    private Boolean isApproved;

    /**
     * Who approved the action
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_admin_id")
    private User approvedByAdmin;

    /**
     * When the action was approved
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Whether this action can be reverted
     */
    @Column(name = "is_reversible", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isReversible = false;

    /**
     * Whether this action has been reverted
     */
    @Column(name = "is_reverted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isReverted = false;

    /**
     * When the action was reverted
     */
    @Column(name = "reverted_at")
    private LocalDateTime revertedAt;

    /**
     * Who reverted the action
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reverted_by_admin_id")
    private User revertedByAdmin;

    /**
     * Reason for reverting
     */
    @Size(max = 500)
    @Column(name = "revert_reason", length = 500)
    private String revertReason;

    @PrePersist
    protected void onCreate() {
        if (this.actionTimestamp == null) {
            this.actionTimestamp = LocalDateTime.now();
        }
    }

    /**
     * Checks if the action is critical
     */
    public boolean isCritical() {
        return "CRITICAL".equals(this.severityLevel);
    }

    /**
     * Checks if the action can be reverted
     */
    public boolean canBeReverted() {
        return this.isReversible && !this.isReverted && this.isSuccessful;
    }

    /**
     * Reverts the action
     */
    public void revert(User revertedBy, String reason) {
        if (!canBeReverted()) {
            throw new IllegalStateException("This action cannot be reverted");
        }
        
        this.isReverted = true;
        this.revertedAt = LocalDateTime.now();
        this.revertedByAdmin = revertedBy;
        this.revertReason = reason;
    }

    /**
     * Approves the action
     */
    public void approve(User approver) {
        if (!this.requiresApproval) {
            throw new IllegalStateException("This action does not require approval");
        }
        
        this.isApproved = true;
        this.approvedAt = LocalDateTime.now();
        this.approvedByAdmin = approver;
    }
}