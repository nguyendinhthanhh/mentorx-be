package com.mentorx.api.feature.matching.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.system.entity.Category;
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
import java.util.Map;

/**
 * Entity representing users waiting in queue for quick mentor matching
 * Manages real-time matching for immediate support requests
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "quick_match_queue", indexes = {
    @Index(name = "idx_quick_match_user_id", columnList = "user_id"),
    @Index(name = "idx_quick_match_category_id", columnList = "category_id"),
    @Index(name = "idx_quick_match_status", columnList = "status"),
    @Index(name = "idx_quick_match_priority", columnList = "priority_score DESC, created_at ASC"),
    @Index(name = "idx_quick_match_created", columnList = "created_at ASC"),
    @Index(name = "idx_quick_match_expires", columnList = "expires_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class QuickMatchQueue extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Current status of the match request
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    private String status = "WAITING"; // WAITING, MATCHED, EXPIRED, CANCELLED

    /**
     * Priority score for queue ordering (higher = more priority)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "priority_score", precision = 5, scale = 2, columnDefinition = "DECIMAL(5,2) DEFAULT 1.00")
    private BigDecimal priorityScore = new BigDecimal("1.00");

    /**
     * Brief description of what user needs help with
     */
    @NotNull
    @Size(max = 500)
    @Column(name = "help_description", nullable = false, length = 500)
    private String helpDescription;

    /**
     * Estimated session duration in minutes
     */
    @Column(name = "estimated_duration_minutes", columnDefinition = "INTEGER DEFAULT 30")
    private Integer estimatedDurationMinutes = 30;

    /**
     * Maximum rate user is willing to pay (in MXC per hour)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "max_rate_mxc", precision = 10, scale = 2)
    private BigDecimal maxRateMxc;

    /**
     * Preferred language for communication
     */
    @Size(max = 10)
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage;

    /**
     * User's timezone
     */
    @Size(max = 50)
    @Column(name = "user_timezone", length = 50)
    private String userTimezone;

    /**
     * When this request expires if not matched
     */
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * ID of matched mentor (if any)
     */
    @Column(name = "matched_mentor_id")
    private Long matchedMentorId;

    /**
     * When the match was made
     */
    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    /**
     * Number of match attempts made
     */
    @Column(name = "match_attempts", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer matchAttempts = 0;

    /**
     * Last time a match attempt was made
     */
    @Column(name = "last_match_attempt_at")
    private LocalDateTime lastMatchAttemptAt;

    /**
     * Additional requirements or preferences as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "requirements", columnDefinition = "jsonb")
    private Map<String, Object> requirements;

    /**
     * Reason for cancellation or failure
     */
    @Size(max = 200)
    @Column(name = "cancellation_reason", length = 200)
    private String cancellationReason;

    /**
     * When the request was cancelled or completed
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Device type used for the request
     */
    @Size(max = 20)
    @Column(name = "device_type", length = 20)
    private String deviceType;

    /**
     * Whether user is currently online
     */
    @Column(name = "is_user_online", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isUserOnline = true;

    /**
     * Last time user was seen online
     */
    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @PrePersist
    protected void onCreate() {
        if (this.expiresAt == null) {
            // Default expiration: 30 minutes from creation
            this.expiresAt = LocalDateTime.now().plusMinutes(30);
        }
        if (this.lastSeenAt == null) {
            this.lastSeenAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if this queue entry has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Checks if this queue entry is still active
     */
    public boolean isActive() {
        return "WAITING".equals(this.status) && !isExpired();
    }
}