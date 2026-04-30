package com.mentorx.api.feature.matching.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.system.entity.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing user interest profile for matching algorithms
 * Tracks user preferences, interests, and behavior patterns
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_interest_profiles", indexes = {
    @Index(name = "idx_user_interest_user_id", columnList = "user_id"),
    @Index(name = "idx_user_interest_category_id", columnList = "category_id"),
    @Index(name = "idx_user_interest_score", columnList = "interest_score DESC"),
    @Index(name = "idx_user_interest_updated", columnList = "last_updated DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserInterestProfile extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Interest score from 0.0 to 1.0 indicating user's interest level
     */
    @NotNull
    @DecimalMin(value = "0.0", message = "Interest score must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Interest score must not exceed 1.0")
    @Column(name = "interest_score", precision = 5, scale = 4, nullable = false)
    private BigDecimal interestScore;

    /**
     * Number of interactions user had with this category
     */
    @Column(name = "interaction_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer interactionCount = 0;

    /**
     * Total time spent on content in this category (in minutes)
     */
    @Column(name = "time_spent_minutes", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer timeSpentMinutes = 0;

    /**
     * Last time user interacted with content in this category
     */
    @Column(name = "last_interaction_at")
    private LocalDateTime lastInteractionAt;

    /**
     * When this interest profile was last updated
     */
    @NotNull
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    /**
     * Decay factor for interest score over time
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "decay_factor", precision = 5, scale = 4, columnDefinition = "DECIMAL(5,4) DEFAULT 0.9500")
    private BigDecimal decayFactor = new BigDecimal("0.9500");

    /**
     * Whether this interest is explicitly set by user or inferred
     */
    @Column(name = "is_explicit", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isExplicit = false;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}