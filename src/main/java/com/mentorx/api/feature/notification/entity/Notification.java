package com.mentorx.api.feature.notification.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.notification.enums.NotificationType;
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
 * Entity representing notifications sent to users
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user_id", columnList = "user_id"),
    @Index(name = "idx_notification_type", columnList = "notification_type"),
    @Index(name = "idx_notification_read", columnList = "is_read, created_at DESC"),
    @Index(name = "idx_notification_user_read", columnList = "user_id, is_read, created_at DESC"),
    @Index(name = "idx_notification_created", columnList = "created_at DESC"),
    @Index(name = "idx_notification_reference", columnList = "reference_type, reference_id"),
    @Index(name = "idx_notification_priority", columnList = "priority_level DESC, created_at DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Type of notification
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    /**
     * Title of the notification
     */
    @NotNull
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Message content
     */
    @NotNull
    @Size(max = 1000)
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    /**
     * Reference to related entity
     */
    @Column(name = "reference_id")
    private java.util.UUID referenceId;

    /**
     * Type of the referenced entity
     */
    @Size(max = 50)
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * Action URL/deep link
     */
    @Size(max = 500)
    @Column(name = "action_url", length = 500)
    private String actionUrl;

    /**
     * Icon or image URL
     */
    @Size(max = 500)
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    /**
     * Priority level (1 = highest, 5 = lowest)
     */
    @Column(name = "priority_level", nullable = false, columnDefinition = "INTEGER DEFAULT 3")
    private Integer priorityLevel = 3;

    /**
     * Whether this notification has been read
     */
    @Column(name = "is_read", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isRead = false;

    /**
     * When the notification was read
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Whether this notification has been delivered
     */
    @Column(name = "is_delivered", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDelivered = false;

    /**
     * When the notification was delivered
     */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /**
     * Whether this notification was sent via push
     */
    @Column(name = "sent_via_push", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean sentViaPush = false;

    /**
     * When push notification was sent
     */
    @Column(name = "push_sent_at")
    private LocalDateTime pushSentAt;

    /**
     * Whether push notification was successful
     */
    @Column(name = "push_successful")
    private Boolean pushSuccessful;

    /**
     * Whether this notification was sent via email
     */
    @Column(name = "sent_via_email", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean sentViaEmail = false;

    /**
     * When email notification was sent
     */
    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    /**
     * Whether email notification was successful
     */
    @Column(name = "email_successful")
    private Boolean emailSuccessful;

    /**
     * Whether this notification was sent via SMS
     */
    @Column(name = "sent_via_sms", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean sentViaSms = false;

    /**
     * When SMS notification was sent
     */
    @Column(name = "sms_sent_at")
    private LocalDateTime smsSentAt;

    /**
     * Whether SMS notification was successful
     */
    @Column(name = "sms_successful")
    private Boolean smsSuccessful;

    /**
     * Additional data as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    private Map<String, Object> data;

    /**
     * Category for grouping notifications
     */
    @Size(max = 30)
    @Column(name = "category", length = 30)
    private String category;

    /**
     * Group ID for related notifications
     */
    @Size(max = 100)
    @Column(name = "group_id", length = 100)
    private String groupId;

    /**
     * Whether this notification can be dismissed
     */
    @Column(name = "is_dismissible", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isDismissible = true;

    /**
     * Whether this notification has been dismissed
     */
    @Column(name = "is_dismissed", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDismissed = false;

    /**
     * When the notification was dismissed
     */
    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    /**
     * Whether this notification requires action
     */
    @Column(name = "requires_action", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean requiresAction = false;

    /**
     * Whether action has been taken
     */
    @Column(name = "action_taken", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean actionTaken = false;

    /**
     * When action was taken
     */
    @Column(name = "action_taken_at")
    private LocalDateTime actionTakenAt;

    /**
     * Expiration date for the notification
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Whether this notification has expired
     */
    @Column(name = "is_expired", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isExpired = false;

    /**
     * Sender user (if applicable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id")
    private User senderUser;

    /**
     * Language of the notification
     */
    @Size(max = 10)
    @Column(name = "language", length = 10)
    private String language;

    /**
     * Template ID used to generate this notification
     */
    @Size(max = 100)
    @Column(name = "template_id", length = 100)
    private String templateId;

    /**
     * Number of retry attempts for delivery
     */
    @Column(name = "retry_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer retryCount = 0;

    /**
     * Last error message if delivery failed
     */
    @Size(max = 500)
    @Column(name = "last_error", length = 500)
    private String lastError;

    /**
     * Marks the notification as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Marks the notification as delivered
     */
    public void markAsDelivered() {
        this.isDelivered = true;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * Dismisses the notification
     */
    public void dismiss() {
        if (!this.isDismissible) {
            throw new IllegalStateException("This notification cannot be dismissed");
        }
        this.isDismissed = true;
        this.dismissedAt = LocalDateTime.now();
    }

    /**
     * Marks action as taken
     */
    public void markActionTaken() {
        this.actionTaken = true;
        this.actionTakenAt = LocalDateTime.now();
    }

    /**
     * Checks if the notification has expired
     */
    public boolean hasExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Checks if the notification is high priority
     */
    public boolean isHighPriority() {
        return this.priorityLevel != null && this.priorityLevel <= 2;
    }

    /**
     * Checks if the notification should be displayed
     */
    public boolean shouldDisplay() {
        return !this.isDismissed && !this.isExpired && !hasExpired();
    }

    /**
     * Records push notification attempt
     */
    public void recordPushAttempt(boolean successful, String error) {
        this.sentViaPush = true;
        this.pushSentAt = LocalDateTime.now();
        this.pushSuccessful = successful;
        if (!successful) {
            this.lastError = error;
            this.retryCount++;
        }
    }

    /**
     * Records email notification attempt
     */
    public void recordEmailAttempt(boolean successful, String error) {
        this.sentViaEmail = true;
        this.emailSentAt = LocalDateTime.now();
        this.emailSuccessful = successful;
        if (!successful) {
            this.lastError = error;
            this.retryCount++;
        }
    }
}