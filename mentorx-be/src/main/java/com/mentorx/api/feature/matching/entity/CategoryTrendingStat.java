package com.mentorx.api.feature.matching.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.system.entity.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing trending statistics for categories
 * Used to identify popular categories and drive recommendations
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "category_trending_stats", indexes = {
    @Index(name = "idx_trending_category_id", columnList = "category_id"),
    @Index(name = "idx_trending_date", columnList = "stat_date DESC"),
    @Index(name = "idx_trending_score", columnList = "trending_score DESC"),
    @Index(name = "idx_trending_category_date", columnList = "category_id, stat_date DESC"),
    @Index(name = "idx_trending_active", columnList = "stat_date DESC, trending_score DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTrendingStat extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Date for which these statistics are calculated
     */
    @NotNull
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    /**
     * Number of job postings in this category
     */
    @Min(value = 0)
    @Column(name = "job_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer jobCount = 0;

    /**
     * Number of course enrollments in this category
     */
    @Min(value = 0)
    @Column(name = "course_enrollment_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer courseEnrollmentCount = 0;

    /**
     * Number of mentor profiles in this category
     */
    @Min(value = 0)
    @Column(name = "mentor_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer mentorCount = 0;

    /**
     * Number of user searches in this category
     */
    @Min(value = 0)
    @Column(name = "search_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer searchCount = 0;

    /**
     * Number of user views/interactions in this category
     */
    @Min(value = 0)
    @Column(name = "view_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer viewCount = 0;

    /**
     * Number of successful matches/contracts in this category
     */
    @Min(value = 0)
    @Column(name = "match_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer matchCount = 0;

    /**
     * Number of quick support requests in this category
     */
    @Min(value = 0)
    @Column(name = "quick_support_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer quickSupportCount = 0;

    /**
     * Growth rate compared to previous period (percentage)
     */
    @Column(name = "growth_rate", precision = 5, scale = 2)
    private java.math.BigDecimal growthRate;

    /**
     * Overall trending score (calculated from various factors)
     */
    @Min(value = 0)
    @Column(name = "trending_score", precision = 8, scale = 2, nullable = false, columnDefinition = "DECIMAL(8,2) DEFAULT 0.00")
    private java.math.BigDecimal trendingScore = java.math.BigDecimal.ZERO;

    /**
     * Rank among all categories for this date
     */
    @Min(value = 1)
    @Column(name = "trending_rank")
    private Integer trendingRank;

    /**
     * Change in rank compared to previous period
     */
    @Column(name = "rank_change")
    private Integer rankChange;

    /**
     * Average rating for mentors in this category
     */
    @Column(name = "average_mentor_rating", precision = 3, scale = 2)
    private java.math.BigDecimal averageMentorRating;

    /**
     * Average hourly rate for mentors in this category (in MXC)
     */
    @Column(name = "average_hourly_rate", precision = 10, scale = 2)
    private java.math.BigDecimal averageHourlyRate;

    /**
     * Number of new users who showed interest in this category
     */
    @Min(value = 0)
    @Column(name = "new_user_interest_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer newUserInterestCount = 0;

    /**
     * When these statistics were calculated
     */
    @NotNull
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    /**
     * Version of the trending algorithm used
     */
    @Column(name = "algorithm_version", length = 20)
    private String algorithmVersion;

    @PrePersist
    protected void onCreate() {
        if (this.calculatedAt == null) {
            this.calculatedAt = LocalDateTime.now();
        }
        if (this.statDate == null) {
            this.statDate = LocalDate.now();
        }
    }

    /**
     * Calculates trending score based on various metrics
     */
    public void calculateTrendingScore() {
        // Simple trending score calculation - can be enhanced with more sophisticated algorithms
        double score = 0.0;
        
        // Weight different metrics
        score += (jobCount != null ? jobCount : 0) * 2.0;           // Jobs are important
        score += (searchCount != null ? searchCount : 0) * 1.5;     // User interest
        score += (viewCount != null ? viewCount : 0) * 1.0;         // Engagement
        score += (matchCount != null ? matchCount : 0) * 3.0;       // Successful outcomes
        score += (quickSupportCount != null ? quickSupportCount : 0) * 2.5; // Immediate needs
        score += (courseEnrollmentCount != null ? courseEnrollmentCount : 0) * 1.8; // Learning interest
        
        // Apply growth rate multiplier if available
        if (growthRate != null && growthRate.compareTo(java.math.BigDecimal.ZERO) > 0) {
            score *= (1.0 + growthRate.doubleValue() / 100.0);
        }
        
        this.trendingScore = java.math.BigDecimal.valueOf(score);
    }
}