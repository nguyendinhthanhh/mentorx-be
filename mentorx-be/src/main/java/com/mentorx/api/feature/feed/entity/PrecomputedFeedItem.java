package com.mentorx.api.feature.feed.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.FeedItemType;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing precomputed personalized feed items
 * Stores recommendations calculated by the matching engine for fast retrieval
 * Items expire after 24 hours and are recalculated by background jobs
 * 
 * Note: Uses entity name "PersonalizedFeedItem" to avoid conflict with
 * com.mentorx.api.feature.matching.entity.PrecomputedFeedItem
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Entity(name = "PersonalizedFeedItem")
@Table(name = "precomputed_feed_items", indexes = {
    @Index(name = "idx_precomputed_feed_user_id", columnList = "user_id"),
    @Index(name = "idx_precomputed_feed_user_computed", columnList = "user_id, computed_at DESC"),
    @Index(name = "idx_precomputed_feed_user_expires", columnList = "user_id, expires_at"),
    @Index(name = "idx_precomputed_feed_user_type_score", columnList = "user_id, item_type, match_score DESC"),
    @Index(name = "idx_precomputed_feed_expires", columnList = "expires_at")
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

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private FeedItemType itemType;

    @NotNull
    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    /**
     * Match score percentage (0-100) calculated using formula:
     * (skillMatch * 0.6) + (levelMatch * 0.3) + (ratingBonus * 0.1)
     */
    @NotNull
    @DecimalMin(value = "0.00", message = "Match score must be at least 0.00")
    @DecimalMax(value = "100.00", message = "Match score must not exceed 100.00")
    @Column(name = "match_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal matchScore;

    /**
     * Timestamp when this feed item was computed
     */
    @NotNull
    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    /**
     * Expiration timestamp (typically computed_at + 24 hours)
     * Expired items should be filtered out from queries
     */
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Additional metadata about the recommendation
     * Example: {"matching_skills": ["Java", "Spring Boot"], "level_match": true, "rating": 4.8}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Check if this feed item has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if this feed item is still valid (not expired)
     */
    public boolean isValid() {
        return !isExpired();
    }

    @PrePersist
    protected void onCreate() {
        if (computedAt == null) {
            computedAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            // Default expiration: 24 hours after computation
            expiresAt = computedAt.plusHours(24);
        }
    }
}
