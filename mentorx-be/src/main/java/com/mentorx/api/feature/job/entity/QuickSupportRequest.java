package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.job.enums.QuickSupportStatus;
import com.mentorx.api.feature.system.entity.Category;
import com.mentorx.api.feature.user.entity.User;
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
import java.time.LocalDateTime;

/**
 * Entity representing quick support requests for immediate help
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "quick_support_requests", indexes = {
    @Index(name = "idx_quick_support_client_id", columnList = "client_id"),
    @Index(name = "idx_quick_support_mentor_id", columnList = "mentor_id"),
    @Index(name = "idx_quick_support_category_id", columnList = "category_id"),
    @Index(name = "idx_quick_support_status", columnList = "status"),
    @Index(name = "idx_quick_support_created", columnList = "created_at DESC"),
    @Index(name = "idx_quick_support_urgency", columnList = "urgency_level DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class QuickSupportRequest extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private User mentor;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Request status
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QuickSupportStatus status = QuickSupportStatus.PENDING;

    /**
     * Brief description of the issue
     */
    @NotNull
    @Size(max = 500)
    @Column(name = "issue_description", nullable = false, length = 500)
    private String issueDescription;

    /**
     * Detailed description
     */
    @Size(max = 2000)
    @Column(name = "detailed_description", length = 2000)
    private String detailedDescription;

    /**
     * Urgency level (1-5, 5 being most urgent)
     */
    @NotNull
    @Min(value = 1)
    @Column(name = "urgency_level", nullable = false)
    private Integer urgencyLevel = 3;

    /**
     * Estimated duration in minutes
     */
    @Column(name = "estimated_duration_minutes", columnDefinition = "INTEGER DEFAULT 30")
    private Integer estimatedDurationMinutes = 30;

    /**
     * Maximum rate willing to pay (in MXC per hour)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "max_rate_mxc", precision = 8, scale = 2)
    private BigDecimal maxRateMxc;

    /**
     * Actual rate agreed (in MXC per hour)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "agreed_rate_mxc", precision = 8, scale = 2)
    private BigDecimal agreedRateMxc;

    /**
     * Total amount charged
     */
    @DecimalMin(value = "0.0")
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * When mentor was matched
     */
    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    /**
     * When session started
     */
    @Column(name = "session_started_at")
    private LocalDateTime sessionStartedAt;

    /**
     * When session ended
     */
    @Column(name = "session_ended_at")
    private LocalDateTime sessionEndedAt;

    /**
     * Actual session duration in minutes
     */
    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    /**
     * When request expires
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Chat room ID for this session
     */
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    /**
     * Session notes from mentor
     */
    @Size(max = 2000)
    @Column(name = "session_notes", length = 2000)
    private String sessionNotes;

    /**
     * Client feedback
     */
    @Size(max = 1000)
    @Column(name = "client_feedback", length = 1000)
    private String clientFeedback;

    /**
     * Client rating (1-5)
     */
    @DecimalMin(value = "1.0")
    @Column(name = "client_rating", precision = 2, scale = 1)
    private BigDecimal clientRating;

    /**
     * Whether issue was resolved
     */
    @Column(name = "issue_resolved")
    private Boolean issueResolved;

    /**
     * Preferred language
     */
    @Size(max = 10)
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage;

    /**
     * Screen sharing required
     */
    @Column(name = "screen_sharing_required", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean screenSharingRequired = false;

    /**
     * Voice call required
     */
    @Column(name = "voice_call_required", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean voiceCallRequired = false;

    /**
     * Number of match attempts
     */
    @Column(name = "match_attempts", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer matchAttempts = 0;

    /**
     * Cancellation reason
     */
    @Size(max = 200)
    @Column(name = "cancellation_reason", length = 200)
    private String cancellationReason;

    /**
     * When cancelled
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @PrePersist
    protected void onCreate() {
        if (this.expiresAt == null) {
            // Default expiration: 30 minutes
            this.expiresAt = LocalDateTime.now().plusMinutes(30);
        }
    }

    /**
     * Matches with a mentor
     */
    public void matchWithMentor(User mentor, BigDecimal agreedRate) {
        this.mentor = mentor;
        this.status = QuickSupportStatus.MATCHED;
        this.matchedAt = LocalDateTime.now();
        this.agreedRateMxc = agreedRate;
    }

    /**
     * Starts the session
     */
    public void startSession(Long chatRoomId) {
        this.status = QuickSupportStatus.IN_PROGRESS;
        this.sessionStartedAt = LocalDateTime.now();
        this.chatRoomId = chatRoomId;
    }

    /**
     * Completes the session
     */
    public void completeSession(String notes, BigDecimal totalAmount) {
        this.status = QuickSupportStatus.COMPLETED;
        this.sessionEndedAt = LocalDateTime.now();
        this.sessionNotes = notes;
        this.totalAmount = totalAmount;
        
        if (this.sessionStartedAt != null) {
            long minutes = java.time.Duration.between(this.sessionStartedAt, this.sessionEndedAt).toMinutes();
            this.actualDurationMinutes = (int) minutes;
        }
    }

    /**
     * Cancels the request
     */
    public void cancel(String reason) {
        this.status = QuickSupportStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    /**
     * Checks if request has expired
     */
    public boolean hasExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Records client feedback
     */
    public void recordFeedback(String feedback, BigDecimal rating, boolean resolved) {
        this.clientFeedback = feedback;
        this.clientRating = rating;
        this.issueResolved = resolved;
    }
}