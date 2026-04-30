package com.mentorx.api.feature.user.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.enums.BadgeType;
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
 * Entity representing badges earned by users
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_badges", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "badge_code"}),
       indexes = {
    @Index(name = "idx_user_badge_user_id", columnList = "user_id"),
    @Index(name = "idx_user_badge_type", columnList = "badge_type"),
    @Index(name = "idx_user_badge_code", columnList = "badge_code"),
    @Index(name = "idx_user_badge_earned", columnList = "earned_at DESC"),
    @Index(name = "idx_user_badge_featured", columnList = "is_featured")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Unique code for the badge
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "badge_code", nullable = false, length = 50)
    private String badgeCode;

    /**
     * Type of badge
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false, length = 30)
    private BadgeType badgeType;

    /**
     * Display name of the badge
     */
    @NotNull
    @Size(max = 100)
    @Column(name = "badge_name", nullable = false, length = 100)
    private String badgeName;

    /**
     * Description of the badge
     */
    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Icon/image URL for the badge
     */
    @Size(max = 500)
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    /**
     * Badge level/tier (bronze, silver, gold, platinum, etc.)
     */
    @Size(max = 20)
    @Column(name = "badge_level", length = 20)
    private String badgeLevel;

    /**
     * When the badge was earned
     */
    @NotNull
    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    /**
     * Whether this badge is featured on profile
     */
    @Column(name = "is_featured", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isFeatured = false;

    /**
     * Whether this badge is visible on profile
     */
    @Column(name = "is_visible", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isVisible = true;

    /**
     * Display order for featured badges
     */
    @Column(name = "display_order")
    private Integer displayOrder;

    /**
     * Rarity level (1-5, 5 being rarest)
     */
    @Column(name = "rarity_level", columnDefinition = "INTEGER DEFAULT 1")
    private Integer rarityLevel = 1;

    /**
     * Points/score associated with this badge
     */
    @Column(name = "points", columnDefinition = "INTEGER DEFAULT 0")
    private Integer points = 0;

    /**
     * Criteria that was met to earn this badge
     */
    @Size(max = 500)
    @Column(name = "criteria_met", length = 500)
    private String criteriaMet;

    /**
     * Reference to the achievement/event that triggered the badge
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * Type of the reference entity
     */
    @Size(max = 50)
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * Additional metadata as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Expiration date (for time-limited badges)
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Whether this badge has expired
     */
    @Column(name = "is_expired", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isExpired = false;

    /**
     * Whether this badge can be revoked
     */
    @Column(name = "is_revocable", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isRevocable = false;

    /**
     * Whether this badge has been revoked
     */
    @Column(name = "is_revoked", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isRevoked = false;

    /**
     * When the badge was revoked
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Reason for revocation
     */
    @Size(max = 200)
    @Column(name = "revoke_reason", length = 200)
    private String revokeReason;

    /**
     * Who revoked the badge
     */
    @Column(name = "revoked_by_admin_id")
    private Long revokedByAdminId;

    /**
     * Number of times this badge has been viewed
     */
    @Column(name = "view_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer viewCount = 0;

    /**
     * Whether this is a limited edition badge
     */
    @Column(name = "is_limited_edition", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isLimitedEdition = false;

    /**
     * Edition number (for limited edition badges)
     */
    @Column(name = "edition_number")
    private Integer editionNumber;

    /**
     * Total number of editions
     */
    @Column(name = "total_editions")
    private Integer totalEditions;

    /**
     * Category for grouping badges
     */
    @Size(max = 50)
    @Column(name = "category", length = 50)
    private String category;

    @PrePersist
    protected void onCreate() {
        if (this.earnedAt == null) {
            this.earnedAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if the badge has expired
     */
    public boolean hasExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Checks if the badge is currently valid
     */
    public boolean isValid() {
        return !this.isRevoked && !this.isExpired && !hasExpired();
    }

    /**
     * Revokes the badge
     */
    public void revoke(Long adminId, String reason) {
        if (!this.isRevocable) {
            throw new IllegalStateException("This badge cannot be revoked");
        }
        
        this.isRevoked = true;
        this.revokedAt = LocalDateTime.now();
        this.revokedByAdminId = adminId;
        this.revokeReason = reason;
    }

    /**
     * Marks the badge as expired
     */
    public void markAsExpired() {
        this.isExpired = true;
    }

    /**
     * Increments view count
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * Gets display text for limited edition
     */
    public String getLimitedEditionText() {
        if (!this.isLimitedEdition || this.editionNumber == null) {
            return null;
        }
        
        if (this.totalEditions != null) {
            return String.format("#%d of %d", this.editionNumber, this.totalEditions);
        }
        
        return String.format("#%d", this.editionNumber);
    }
}