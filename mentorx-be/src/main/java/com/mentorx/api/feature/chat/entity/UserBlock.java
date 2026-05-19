package com.mentorx.api.feature.chat.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing user blocking relationships for privacy control
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_blocks", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_user_id", "blocked_user_id"}),
       indexes = {
    @Index(name = "idx_user_block_blocker", columnList = "blocker_user_id"),
    @Index(name = "idx_user_block_blocked", columnList = "blocked_user_id"),
    @Index(name = "idx_user_block_active", columnList = "is_active"),
    @Index(name = "idx_user_block_created", columnList = "blocked_at DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserBlock extends BaseEntity {

    /**
     * User who is doing the blocking
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_user_id", nullable = false)
    private User blockerUser;

    /**
     * User who is being blocked
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_user_id", nullable = false)
    private User blockedUser;

    /**
     * When the block was created
     */
    @NotNull
    @Column(name = "blocked_at", nullable = false)
    private LocalDateTime blockedAt;

    /**
     * Whether this block is currently active
     */
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    /**
     * Reason for blocking
     */
    @Size(max = 500)
    @Column(name = "block_reason", length = 500)
    private String blockReason;

    /**
     * Type of block (MESSAGES, PROFILE_VIEW, ALL)
     */
    @Size(max = 20)
    @Column(name = "block_type", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'ALL'")
    private String blockType = "ALL";

    /**
     * When the block was lifted (if applicable)
     */
    @Column(name = "unblocked_at")
    private LocalDateTime unblockedAt;

    /**
     * Reason for unblocking
     */
    @Size(max = 200)
    @Column(name = "unblock_reason", length = 200)
    private String unblockReason;

    /**
     * Whether this block was reported to admins
     */
    @Column(name = "is_reported", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isReported = false;

    /**
     * When this block was reported
     */
    @Column(name = "reported_at")
    private LocalDateTime reportedAt;

    /**
     * ID of the report (if any)
     */
    @Column(name = "report_id")
    private java.util.UUID reportId;

    /**
     * Whether this is a mutual block
     */
    @Column(name = "is_mutual", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isMutual = false;

    /**
     * Expiration date for temporary blocks
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Whether this is a temporary block
     */
    @Column(name = "is_temporary", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isTemporary = false;

    /**
     * Number of times this user has been blocked by the blocker
     */
    @Column(name = "block_count", nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    private Integer blockCount = 1;

    /**
     * Last interaction before blocking
     */
    @Column(name = "last_interaction_at")
    private LocalDateTime lastInteractionAt;

    /**
     * Type of last interaction
     */
    @Size(max = 50)
    @Column(name = "last_interaction_type", length = 50)
    private String lastInteractionType;

    @PrePersist
    protected void onCreate() {
        if (this.blockedAt == null) {
            this.blockedAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if the block is currently effective
     */
    public boolean isEffective() {
        if (!this.isActive) {
            return false;
        }
        
        if (this.isTemporary && this.expiresAt != null) {
            return LocalDateTime.now().isBefore(this.expiresAt);
        }
        
        return true;
    }

    /**
     * Checks if the block has expired
     */
    public boolean hasExpired() {
        return this.isTemporary && 
               this.expiresAt != null && 
               LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Unblocks the user
     */
    public void unblock(String reason) {
        this.isActive = false;
        this.unblockedAt = LocalDateTime.now();
        this.unblockReason = reason;
    }

    /**
     * Checks if specific interaction type is blocked
     */
    public boolean isInteractionBlocked(String interactionType) {
        if (!isEffective()) {
            return false;
        }
        
        switch (this.blockType) {
            case "ALL":
                return true;
            case "MESSAGES":
                return "MESSAGE".equals(interactionType) || "CHAT".equals(interactionType);
            case "PROFILE_VIEW":
                return "PROFILE_VIEW".equals(interactionType);
            default:
                return false;
        }
    }

    /**
     * Extends the block duration (for temporary blocks)
     */
    public void extendBlock(LocalDateTime newExpirationDate) {
        if (this.isTemporary) {
            this.expiresAt = newExpirationDate;
        }
    }

    /**
     * Converts to permanent block
     */
    public void makePermanent() {
        this.isTemporary = false;
        this.expiresAt = null;
    }
}