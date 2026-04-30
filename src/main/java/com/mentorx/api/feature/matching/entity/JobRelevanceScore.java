package com.mentorx.api.feature.matching.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.job.entity.Job;
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
 * Entity representing precomputed relevance scores between users and job postings
 * Used for fast retrieval of personalized job recommendations
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "job_relevance_scores", indexes = {
    @Index(name = "idx_job_relevance_user_id", columnList = "user_id"),
    @Index(name = "idx_job_relevance_job_id", columnList = "job_id"),
    @Index(name = "idx_job_relevance_score", columnList = "relevance_score DESC"),
    @Index(name = "idx_job_relevance_user_score", columnList = "user_id, relevance_score DESC"),
    @Index(name = "idx_job_relevance_computed", columnList = "computed_at DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class JobRelevanceScore extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    /**
     * Overall relevance score from 0.0 to 1.0
     */
    @NotNull
    @DecimalMin(value = "0.0", message = "Relevance score must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Relevance score must not exceed 1.0")
    @Column(name = "relevance_score", precision = 5, scale = 4, nullable = false)
    private BigDecimal relevanceScore;

    /**
     * Skill match score (0.0 - 1.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "skill_match", precision = 5, scale = 4)
    private BigDecimal skillMatch;

    /**
     * Experience level match score (0.0 - 1.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "experience_match", precision = 5, scale = 4)
    private BigDecimal experienceMatch;

    /**
     * Budget/rate compatibility score (0.0 - 1.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "budget_match", precision = 5, scale = 4)
    private BigDecimal budgetMatch;

    /**
     * Category interest match score (0.0 - 1.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "category_match", precision = 5, scale = 4)
    private BigDecimal categoryMatch;

    /**
     * Timeline/availability match score (0.0 - 1.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "timeline_match", precision = 5, scale = 4)
    private BigDecimal timelineMatch;

    /**
     * Geographic/timezone compatibility score (0.0 - 1.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "geographic_match", precision = 5, scale = 4)
    private BigDecimal geographicMatch;

    /**
     * When this relevance score was computed
     */
    @NotNull
    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    /**
     * When this relevance score expires and needs recomputation
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
     * Whether this job has been shown to the user
     */
    @Column(name = "is_shown", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isShown = false;

    /**
     * When this job was first shown to the user
     */
    @Column(name = "shown_at")
    private LocalDateTime shownAt;

    /**
     * Number of times this job has been shown
     */
    @Column(name = "show_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer showCount = 0;

    /**
     * Whether user has interacted with this job (viewed, applied, etc.)
     */
    @Column(name = "has_interaction", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean hasInteraction = false;

    @PrePersist
    protected void onCreate() {
        this.computedAt = LocalDateTime.now();
        if (this.expiresAt == null) {
            // Default expiration: 3 days from computation (jobs are more time-sensitive)
            this.expiresAt = this.computedAt.plusDays(3);
        }
    }
}