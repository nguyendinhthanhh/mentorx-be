package com.mentorx.api.feature.matching.entity;

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
 * Entity representing user interaction events for behavior tracking and learning
 * This table is partitioned by date for performance with large datasets
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_interaction_events", indexes = {
    @Index(name = "idx_interaction_user_id", columnList = "user_id"),
    @Index(name = "idx_interaction_type", columnList = "interaction_type"),
    @Index(name = "idx_interaction_reference", columnList = "reference_type, reference_id"),
    @Index(name = "idx_interaction_timestamp", columnList = "interaction_timestamp DESC"),
    @Index(name = "idx_interaction_user_time", columnList = "user_id, interaction_timestamp DESC"),
    @Index(name = "idx_interaction_session", columnList = "session_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserInteractionEvent extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Type of interaction (view, click, apply, bookmark, share, etc.)
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "interaction_type", nullable = false, length = 50)
    private String interactionType;

    /**
     * ID of the entity being interacted with
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * Type of the referenced entity
     */
    @Size(max = 50)
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * When the interaction occurred
     */
    @NotNull
    @Column(name = "interaction_timestamp", nullable = false)
    private LocalDateTime interactionTimestamp;

    /**
     * Session ID for grouping related interactions
     */
    @Size(max = 100)
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * Duration of interaction in seconds (for time-based events)
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /**
     * Source of the interaction (feed, search, direct, etc.)
     */
    @Size(max = 50)
    @Column(name = "interaction_source", length = 50)
    private String interactionSource;

    /**
     * Device type (mobile, desktop, tablet)
     */
    @Size(max = 20)
    @Column(name = "device_type", length = 20)
    private String deviceType;

    /**
     * User agent string
     */
    @Size(max = 500)
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * IP address of the user
     */
    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Referrer URL
     */
    @Size(max = 500)
    @Column(name = "referrer_url", length = 500)
    private String referrerUrl;

    /**
     * Additional context data as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_data", columnDefinition = "jsonb")
    private Map<String, Object> contextData;

    /**
     * Whether this interaction was successful/completed
     */
    @Column(name = "is_successful", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isSuccessful = true;

    /**
     * Error message if interaction failed
     */
    @Size(max = 500)
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /**
     * A/B test variant if applicable
     */
    @Size(max = 50)
    @Column(name = "ab_test_variant", length = 50)
    private String abTestVariant;

    /**
     * Geographic location (country code)
     */
    @Size(max = 10)
    @Column(name = "country_code", length = 10)
    private String countryCode;

    /**
     * Timezone of the user
     */
    @Size(max = 50)
    @Column(name = "timezone", length = 50)
    private String timezone;

    @PrePersist
    protected void onCreate() {
        if (this.interactionTimestamp == null) {
            this.interactionTimestamp = LocalDateTime.now();
        }
    }
}