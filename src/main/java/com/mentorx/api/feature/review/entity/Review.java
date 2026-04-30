package com.mentorx.api.feature.review.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.review.enums.ReviewTargetType;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
 * Entity representing reviews and ratings for various platform entities
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_review_reviewer_id", columnList = "reviewer_id"),
    @Index(name = "idx_review_target", columnList = "target_type, target_id"),
    @Index(name = "idx_review_rating", columnList = "overall_rating DESC"),
    @Index(name = "idx_review_created", columnList = "created_at DESC"),
    @Index(name = "idx_review_verified", columnList = "is_verified"),
    @Index(name = "idx_review_target_rating", columnList = "target_type, target_id, overall_rating DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {

    /**
     * User who wrote the review
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    /**
     * Type of entity being reviewed
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private ReviewTargetType targetType;

    /**
     * ID of the entity being reviewed
     */
    @NotNull
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /**
     * Overall rating (1-5 stars)
     */
    @NotNull
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    @Column(name = "overall_rating", precision = 2, scale = 1, nullable = false)
    private BigDecimal overallRating;

    /**
     * Communication rating (1-5 stars)
     */
    @DecimalMin(value = "1.0")
    @DecimalMax(value = "5.0")
    @Column(name = "communication_rating", precision = 2, scale = 1)
    private BigDecimal communicationRating;

    /**
     * Quality rating (1-5 stars)
     */
    @DecimalMin(value = "1.0")
    @DecimalMax(value = "5.0")
    @Column(name = "quality_rating", precision = 2, scale = 1)
    private BigDecimal qualityRating;

    /**
     * Timeliness rating (1-5 stars)
     */
    @DecimalMin(value = "1.0")
    @DecimalMax(value = "5.0")
    @Column(name = "timeliness_rating", precision = 2, scale = 1)
    private BigDecimal timelinessRating;

    /**
     * Professionalism rating (1-5 stars)
     */
    @DecimalMin(value = "1.0")
    @DecimalMax(value = "5.0")
    @Column(name = "professionalism_rating", precision = 2, scale = 1)
    private BigDecimal professionalismRating;

    /**
     * Value for money rating (1-5 stars)
     */
    @DecimalMin(value = "1.0")
    @DecimalMax(value = "5.0")
    @Column(name = "value_rating", precision = 2, scale = 1)
    private BigDecimal valueRating;

    /**
     * Written review content
     */
    @Size(max = 2000)
    @Column(name = "review_text", length = 2000)
    private String reviewText;

    /**
     * Title of the review
     */
    @Size(max = 200)
    @Column(name = "review_title", length = 200)
    private String reviewTitle;

    /**
     * Pros mentioned in the review
     */
    @Size(max = 1000)
    @Column(name = "pros", length = 1000)
    private String pros;

    /**
     * Cons mentioned in the review
     */
    @Size(max = 1000)
    @Column(name = "cons", length = 1000)
    private String cons;

    /**
     * Whether this review is verified (reviewer actually used the service)
     */
    @Column(name = "is_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isVerified = false;

    /**
     * When the review was verified
     */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    /**
     * Whether this review is anonymous
     */
    @Column(name = "is_anonymous", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAnonymous = false;

    /**
     * Whether this review is public
     */
    @Column(name = "is_public", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isPublic = true;

    /**
     * Whether this review is featured
     */
    @Column(name = "is_featured", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isFeatured = false;

    /**
     * Number of helpful votes
     */
    @Min(value = 0)
    @Column(name = "helpful_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer helpfulCount = 0;

    /**
     * Number of not helpful votes
     */
    @Min(value = 0)
    @Column(name = "not_helpful_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer notHelpfulCount = 0;

    /**
     * Number of reports for this review
     */
    @Min(value = 0)
    @Column(name = "report_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer reportCount = 0;

    /**
     * Whether this review has been moderated
     */
    @Column(name = "is_moderated", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isModerated = false;

    /**
     * When the review was moderated
     */
    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    /**
     * ID of admin who moderated this review
     */
    @Column(name = "moderated_by_admin_id")
    private Long moderatedByAdminId;

    /**
     * Moderation notes
     */
    @Size(max = 500)
    @Column(name = "moderation_notes", length = 500)
    private String moderationNotes;

    /**
     * Whether this review is hidden
     */
    @Column(name = "is_hidden", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isHidden = false;

    /**
     * Reason for hiding the review
     */
    @Size(max = 200)
    @Column(name = "hidden_reason", length = 200)
    private String hiddenReason;

    /**
     * Language of the review
     */
    @Size(max = 10)
    @Column(name = "language", length = 10)
    private String language;

    /**
     * Additional metadata as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Reference to the contract/transaction this review is for
     */
    @Column(name = "contract_id")
    private Long contractId;

    /**
     * When the reviewed service was completed
     */
    @Column(name = "service_completed_at")
    private LocalDateTime serviceCompletedAt;

    /**
     * Duration of the service in hours
     */
    @DecimalMin(value = "0.0")
    @Column(name = "service_duration_hours", precision = 6, scale = 2)
    private BigDecimal serviceDurationHours;

    /**
     * Amount paid for the service (in MXC)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "service_amount_mxc", precision = 10, scale = 2)
    private BigDecimal serviceAmountMxc;

    /**
     * Whether the reviewer would recommend this service
     */
    @Column(name = "would_recommend", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean wouldRecommend = true;

    /**
     * Response from the reviewed party
     */
    @Size(max = 1000)
    @Column(name = "response_text", length = 1000)
    private String responseText;

    /**
     * When the response was posted
     */
    @Column(name = "response_at")
    private LocalDateTime responseAt;

    /**
     * ID of user who posted the response
     */
    @Column(name = "response_by_user_id")
    private Long responseByUserId;

    /**
     * Calculates helpfulness ratio
     */
    public double getHelpfulnessRatio() {
        int totalVotes = helpfulCount + notHelpfulCount;
        if (totalVotes == 0) {
            return 0.0;
        }
        return (double) helpfulCount / totalVotes;
    }

    /**
     * Checks if review can be edited
     */
    public boolean canBeEdited() {
        if (isModerated || isHidden) {
            return false;
        }
        
        // Allow editing within 24 hours of creation
        return getCreatedAt().isAfter(LocalDateTime.now().minusHours(24));
    }

    /**
     * Checks if review should be displayed publicly
     */
    public boolean shouldDisplayPublicly() {
        return isPublic && !isHidden && (isModerated || reportCount < 3);
    }

    /**
     * Gets display name for reviewer
     */
    public String getReviewerDisplayName() {
        if (isAnonymous) {
            return "Anonymous User";
        }
        return reviewer != null ? reviewer.getDisplayName() : "Unknown User";
    }
}