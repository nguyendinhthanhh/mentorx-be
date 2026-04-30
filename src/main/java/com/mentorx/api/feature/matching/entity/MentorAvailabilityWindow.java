package com.mentorx.api.feature.matching.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.MentorProfile;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity representing mentor availability windows for scheduling and matching
 * Tracks when mentors are available for quick support and scheduled sessions
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "mentor_availability_windows", indexes = {
    @Index(name = "idx_availability_mentor_id", columnList = "mentor_profile_id"),
    @Index(name = "idx_availability_day", columnList = "day_of_week"),
    @Index(name = "idx_availability_time", columnList = "start_time, end_time"),
    @Index(name = "idx_availability_active", columnList = "is_active, mentor_profile_id"),
    @Index(name = "idx_availability_timezone", columnList = "timezone")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MentorAvailabilityWindow extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_profile_id", nullable = false)
    private MentorProfile mentorProfile;

    /**
     * Day of week (1 = Monday, 7 = Sunday)
     */
    @NotNull
    @Min(value = 1, message = "Day of week must be between 1 and 7")
    @Max(value = 7, message = "Day of week must be between 1 and 7")
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    /**
     * Start time of availability window
     */
    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /**
     * End time of availability window
     */
    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Timezone for this availability window
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone;

    /**
     * Whether this availability window is currently active
     */
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    /**
     * Whether mentor accepts quick support requests during this window
     */
    @Column(name = "accepts_quick_support", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean acceptsQuickSupport = true;

    /**
     * Whether mentor accepts scheduled sessions during this window
     */
    @Column(name = "accepts_scheduled_sessions", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean acceptsScheduledSessions = true;

    /**
     * Maximum number of concurrent sessions during this window
     */
    @Min(value = 1)
    @Column(name = "max_concurrent_sessions", columnDefinition = "INTEGER DEFAULT 1")
    private Integer maxConcurrentSessions = 1;

    /**
     * Minimum notice required for booking (in hours)
     */
    @Min(value = 0)
    @Column(name = "min_notice_hours", columnDefinition = "INTEGER DEFAULT 2")
    private Integer minNoticeHours = 2;

    /**
     * Maximum advance booking allowed (in days)
     */
    @Min(value = 1)
    @Column(name = "max_advance_days", columnDefinition = "INTEGER DEFAULT 30")
    private Integer maxAdvanceDays = 30;

    /**
     * Notes about this availability window
     */
    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * When this availability window was last updated
     */
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
        validateTimeWindow();
    }

    /**
     * Validates that end time is after start time
     */
    @PostLoad
    private void validateTimeWindow() {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }
}