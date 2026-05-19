package com.mentorx.api.feature.matching.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.MentorProfile;
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
 * Entity representing precomputed match scores between users and mentors
 * Used for fast retrieval of personalized mentor recommendations
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "mentor_match_scores", indexes = {
    @Index(name = "idx_mentor_match_user_id", columnList = "user_id"),
    @Index(name = "idx_mentor_match_mentor_id", columnList = "mentor_profile_id"),
    @Index(name = "idx_mentor_match_score", columnList = "match_score DESC"),
    @Index(name = "idx_mentor_match_user_score", columnList = "user_id, match_score DESC"),
    @Index(name = "idx_mentor_match_computed", columnList = "computed_at DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MentorMatchScore extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_profile_id", nullable = false)
    private MentorProfile mentorProfile;

    /**
     * Overall match score from 0.0 to 1.0
     */
    @NotNull
    @DecimalMin(value = "0.0", message = "Match score must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Match score must not exceed 1.0")
    @Column(name = "match_score", precision = 5, scale = 4, nullable = false)
    private BigDecimal matchScore;

    /**
     * Interest compatibility score (0.0 - 1.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "interest_compatibility", precision = 5, scale = 4)
    private BigDecimal interestCompatibility;

    /**
     * Skill level compatibility score (0.0 - 1.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "skill_compatibility", precision = 5, scale = 4)
    private BigDecimal skillCompatibility;

    /**
     * Budget compatibility score (0.0 - 1.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "budget_compatibility", precision = 5, scale = 4)
    private BigDecimal budgetCompatibility;

    /**
     * Availability compatibility score (0.0 - 1.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "availability_compatibility", precision = 5, scale = 4)
    private BigDecimal availabilityCompatibility;

    /**
     * Communication style compatibility score (0.0 - 1.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "communication_compatibility", precision = 5, scale = 4)
    private BigDecimal communicationCompatibility;

    /**
     * Geographic/timezone compatibility score (0.0 - 1.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "geographic_compatibility", precision = 5, scale = 4)
    private BigDecimal geographicCompatibility;

    /**
     * When this match score was computed
     */
    @NotNull
    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    /**
     * When this match score expires and needs recomputation
     */
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Version of the matching algorithm used
     */
    @Column(name = "algorithm_version", length = 20)
    private String algorithmVersion;

    /**
     * Whether this match has been shown to the user
     */
    @Column(name = "is_shown", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isShown = false;

    /**
     * When this match was first shown to the user
     */
    @Column(name = "shown_at")
    private LocalDateTime shownAt;

    /**
     * Number of times this match has been shown
     */
    @Column(name = "show_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer showCount = 0;

    @PrePersist
    protected void onCreate() {
        this.computedAt = LocalDateTime.now();
        if (this.expiresAt == null) {
            // Default expiration: 7 days from computation
            this.expiresAt = this.computedAt.plusDays(7);
        }
    }
}