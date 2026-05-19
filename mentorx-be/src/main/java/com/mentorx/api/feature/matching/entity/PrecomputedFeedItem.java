package com.mentorx.api.feature.matching.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.matching.enums.FeedItemType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
 * Entity representing precomputed feed items for personalized user feeds
 * Contains all types of recommendations and content for fast feed generation
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "precomputed_feed_items", indexes = {
    @Index(name = "idx_feed_item_user_id", columnList = "user_id"),
    @Index(name = "idx_feed_item_type", columnList = "item_type"),
    @Index(name = "idx_feed_item_score", columnList = "relevance_score DESC"),
    @Index(name = "idx_feed_item_user_score", columnList = "user_id, relevance_score DESC"),
    @Index(name = "idx_feed_item_user_type", columnList = "user_id, item_type, relevance_score DESC"),
    @Index(name = "idx_feed_item_computed", columnList = "computed_at DESC"),
    @Index(name = "idx_feed_item_expires", columnList = "expires_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PrecomputedFeedItem extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Type of feed item (mentor, job, course, etc.)
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 30)
    private FeedItemType itemType;

    /**
     * ID of the referenced entity (mentor_id, job_id, course_id, etc.)
     */
    @NotNull
    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    /**
     * Type of the referenced entity for polymorphic references
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "reference_type", nullable = false, length = 50)
    private String referenceType;

    /**
     * Relevance score for this feed item (0.0 - 1.0)
     */
    @NotNull
    @DecimalMin(value = "0.0", message = "Relevance score must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Relevance score must not exceed 1.0")
    @Column(name = "relevance_score", precision = 5, scale = 4, nullable = false)
    private BigDecimal relevanceScore;

    /**
     * Position in the feed (for ordering)
     */
    @Column(name = "feed_position", nullable = false)
    private Integer feedPosition;

    /**
     * Title for display in feed
     */
    @Size(max = 200)
    @Column(name = "display_title", length = 200)
    private String displayTitle;

    /**
     * Description for display in feed
     */
    @Size(max = 500)
    @Column(name = "display_description", length = 500)
    private String displayDescription;

    /**
     * URL for thumbnail/image
     */
    @Size(max = 500)
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /**
     * Additional metadata as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * When this feed item was computed
     */
    @NotNull
    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    /**
     * When this feed item expires and should be removed
     */
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Version of the algorithm used to generate this item
     */
    @Column(name = "algorithm_version", length = 20)
    private String algorithmVersion;

    /**
     * Whether this item has been shown to the user
     */
    @Column(name = "is_shown", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isShown = false;

    /**
     * When this item was first shown to the user
     */
    @Column(name = "shown_at")
    private LocalDateTime shownAt;

    /**
     * Number of times this item has been shown
     */
    @Column(name = "show_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer showCount = 0;

    /**
     * Whether user has interacted with this item
     */
    @Column(name = "has_interaction", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean hasInteraction = false;

    /**
     * Type of interaction (click, view, apply, etc.)
     */
    @Column(name = "interaction_type", length = 30)
    private String interactionType;

    /**
     * When user last interacted with this item
     */
    @Column(name = "last_interaction_at")
    private LocalDateTime lastInteractionAt;

    @PrePersist
    protected void onCreate() {
        this.computedAt = LocalDateTime.now();
        if (this.expiresAt == null) {
            // Default expiration based on item type
            switch (this.itemType) {
                case JOB_RECOMMENDATION:
                case QUICK_SUPPORT:
                    this.expiresAt = this.computedAt.plusDays(3);
                    break;
                case TRENDING_CONTENT:
                case COMMUNITY_UPDATE:
                    this.expiresAt = this.computedAt.plusDays(1);
                    break;
                case FEATURED_MENTOR:
                    this.expiresAt = this.computedAt.plusDays(7);
                    break;
                default:
                    this.expiresAt = this.computedAt.plusDays(7);
            }
        }
    }
}