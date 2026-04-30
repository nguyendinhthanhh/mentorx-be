package com.mentorx.api.feature.review.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing periodic snapshots of user reputation scores
 * Used for tracking reputation changes over time and analytics
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "reputation_snapshots", indexes = {
    @Index(name = "idx_reputation_user_id", columnList = "user_id"),
    @Index(name = "idx_reputation_snapshot_date", columnList = "snapshot_date DESC"),
    @Index(name = "idx_reputation_user_date", columnList = "user_id, snapshot_date DESC"),
    @Index(name = "idx_reputation_score", columnList = "overall_score DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReputationSnapshot extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Date of this snapshot
     */
    @NotNull
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    /**
     * Overall reputation score (0.0 - 100.0)
     */
    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(name = "overall_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal overallScore;

    /**
     * Average rating received (1.0 - 5.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    /**
     * Total number of reviews received
     */
    @Min(value = 0)
    @Column(name = "total_reviews", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer totalReviews = 0;

    /**
     * Number of 5-star reviews
     */
    @Min(value = 0)
    @Column(name = "five_star_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer fiveStarCount = 0;

    /**
     * Number of 4-star reviews
     */
    @Min(value = 0)
    @Column(name = "four_star_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer fourStarCount = 0;

    /**
     * Number of 3-star reviews
     */
    @Min(value = 0)
    @Column(name = "three_star_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer threeStarCount = 0;

    /**
     * Number of 2-star reviews
     */
    @Min(value = 0)
    @Column(name = "two_star_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer twoStarCount = 0;

    /**
     * Number of 1-star reviews
     */
    @Min(value = 0)
    @Column(name = "one_star_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer oneStarCount = 0;

    /**
     * Average communication rating
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(name = "avg_communication_rating", precision = 3, scale = 2)
    private BigDecimal avgCommunicationRating;

    /**
     * Average quality rating
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(name = "avg_quality_rating", precision = 3, scale = 2)
    private BigDecimal avgQualityRating;

    /**
     * Average timeliness rating
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(name = "avg_timeliness_rating", precision = 3, scale = 2)
    private BigDecimal avgTimelinessRating;

    /**
     * Average professionalism rating
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(name = "avg_professionalism_rating", precision = 3, scale = 2)
    private BigDecimal avgProfessionalismRating;

    /**
     * Average value rating
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(name = "avg_value_rating", precision = 3, scale = 2)
    private BigDecimal avgValueRating;

    /**
     * Number of completed projects/contracts
     */
    @Min(value = 0)
    @Column(name = "completed_projects", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer completedProjects = 0;

    /**
     * Success rate (percentage of successful projects)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(name = "success_rate", precision = 5, scale = 2)
    private BigDecimal successRate;

    /**
     * Response rate (percentage of responded inquiries)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(name = "response_rate", precision = 5, scale = 2)
    private BigDecimal responseRate;

    /**
     * Average response time in hours
     */
    @DecimalMin(value = "0.0")
    @Column(name = "avg_response_time_hours", precision = 6, scale = 2)
    private BigDecimal avgResponseTimeHours;

    /**
     * On-time delivery rate (percentage)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(name = "on_time_delivery_rate", precision = 5, scale = 2)
    private BigDecimal onTimeDeliveryRate;

    /**
     * Repeat client rate (percentage)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(name = "repeat_client_rate", precision = 5, scale = 2)
    private BigDecimal repeatClientRate;

    /**
     * Total earnings in this period (in MXC)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "total_earnings_mxc", precision = 12, scale = 2)
    private BigDecimal totalEarningsMxc;

    /**
     * Number of active disputes
     */
    @Min(value = 0)
    @Column(name = "active_disputes", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer activeDisputes = 0;

    /**
     * Number of resolved disputes
     */
    @Min(value = 0)
    @Column(name = "resolved_disputes", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer resolvedDisputes = 0;

    /**
     * Dispute resolution rate (percentage)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(name = "dispute_resolution_rate", precision = 5, scale = 2)
    private BigDecimal disputeResolutionRate;

    /**
     * Reputation rank among all users
     */
    @Min(value = 1)
    @Column(name = "reputation_rank")
    private Integer reputationRank;

    /**
     * Reputation rank within user's category
     */
    @Min(value = 1)
    @Column(name = "category_rank")
    private Integer categoryRank;

    /**
     * Change in overall score from previous snapshot
     */
    @Column(name = "score_change", precision = 5, scale = 2)
    private BigDecimal scoreChange;

    /**
     * Change in rank from previous snapshot
     */
    @Column(name = "rank_change")
    private Integer rankChange;

    /**
     * Number of badges earned
     */
    @Min(value = 0)
    @Column(name = "badge_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer badgeCount = 0;

    /**
     * Trust score (0.0 - 100.0)
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(name = "trust_score", precision = 5, scale = 2)
    private BigDecimal trustScore;

    /**
     * When this snapshot was calculated
     */
    @NotNull
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    /**
     * Version of the reputation algorithm used
     */
    @Column(name = "algorithm_version", length = 20)
    private String algorithmVersion;

    @PrePersist
    protected void onCreate() {
        if (this.calculatedAt == null) {
            this.calculatedAt = LocalDateTime.now();
        }
        if (this.snapshotDate == null) {
            this.snapshotDate = LocalDate.now();
        }
    }

    /**
     * Calculates the percentage of positive reviews (4-5 stars)
     */
    public double getPositiveReviewPercentage() {
        if (totalReviews == 0) {
            return 0.0;
        }
        int positiveReviews = (fiveStarCount != null ? fiveStarCount : 0) + 
                             (fourStarCount != null ? fourStarCount : 0);
        return (double) positiveReviews / totalReviews * 100.0;
    }

    /**
     * Checks if reputation is improving
     */
    public boolean isImproving() {
        return scoreChange != null && scoreChange.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Gets reputation level based on score
     */
    public String getReputationLevel() {
        if (overallScore == null) {
            return "UNRATED";
        }
        
        double score = overallScore.doubleValue();
        if (score >= 90.0) return "EXCELLENT";
        if (score >= 75.0) return "VERY_GOOD";
        if (score >= 60.0) return "GOOD";
        if (score >= 40.0) return "FAIR";
        return "NEEDS_IMPROVEMENT";
    }
}