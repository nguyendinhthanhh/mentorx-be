package com.mentorx.api.feature.user.entity;

import com.mentorx.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing password reset tokens
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
    @Index(name = "idx_password_token_user_id", columnList = "user_id"),
    @Index(name = "idx_password_token_token", columnList = "token"),
    @Index(name = "idx_password_token_expires", columnList = "expires_at"),
    @Index(name = "idx_password_token_used", columnList = "is_used")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Reset token
     */
    @NotNull
    @Size(max = 255)
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    /**
     * When the token expires
     */
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Whether the token has been used
     */
    @Column(name = "is_used", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isUsed = false;

    /**
     * When the token was used
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * IP address from which reset was requested
     */
    @Size(max = 45)
    @Column(name = "request_ip", length = 45)
    private String requestIp;

    /**
     * IP address from which reset was completed
     */
    @Size(max = 45)
    @Column(name = "reset_ip", length = 45)
    private String resetIp;

    /**
     * User agent from reset request
     */
    @Size(max = 500)
    @Column(name = "request_user_agent", length = 500)
    private String requestUserAgent;

    /**
     * User agent from reset completion
     */
    @Size(max = 500)
    @Column(name = "reset_user_agent", length = 500)
    private String resetUserAgent;

    /**
     * Number of reset attempts with this token
     */
    @Column(name = "attempt_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer attemptCount = 0;

    /**
     * Last attempt timestamp
     */
    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    /**
     * Whether this token was invalidated
     */
    @Column(name = "is_invalidated", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isInvalidated = false;

    /**
     * When the token was invalidated
     */
    @Column(name = "invalidated_at")
    private LocalDateTime invalidatedAt;

    /**
     * Reason for invalidation
     */
    @Size(max = 200)
    @Column(name = "invalidation_reason", length = 200)
    private String invalidationReason;

    /**
     * Email address associated with this reset
     */
    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;

    /**
     * Security question answer hash (optional additional verification)
     */
    @Size(max = 255)
    @Column(name = "security_answer_hash", length = 255)
    private String securityAnswerHash;

    /**
     * Whether security question was verified
     */
    @Column(name = "security_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean securityVerified = false;

    @PrePersist
    protected void onCreate() {
        if (this.token == null) {
            this.token = UUID.randomUUID().toString();
        }
        
        if (this.expiresAt == null) {
            // Default expiration: 1 hour for security
            this.expiresAt = LocalDateTime.now().plusHours(1);
        }
    }

    /**
     * Checks if the token has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Checks if the token is valid
     */
    public boolean isValid() {
        return !this.isUsed && !this.isInvalidated && !isExpired();
    }

    /**
     * Marks the token as used
     */
    public void markAsUsed(String resetIp, String resetUserAgent) {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
        this.resetIp = resetIp;
        this.resetUserAgent = resetUserAgent;
    }

    /**
     * Records a reset attempt
     */
    public void recordAttempt() {
        this.attemptCount++;
        this.lastAttemptAt = LocalDateTime.now();
    }

    /**
     * Checks if maximum attempts have been exceeded
     */
    public boolean hasExceededMaxAttempts() {
        return this.attemptCount >= 3; // Maximum 3 attempts for security
    }

    /**
     * Invalidates the token
     */
    public void invalidate(String reason) {
        this.isInvalidated = true;
        this.invalidatedAt = LocalDateTime.now();
        this.invalidationReason = reason;
    }

    /**
     * Marks security question as verified
     */
    public void markSecurityVerified() {
        this.securityVerified = true;
    }

    /**
     * Generates a new token
     */
    public static PasswordResetToken createToken(User user, String email) {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setEmail(email);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        return token;
    }

    /**
     * Checks if the token is about to expire (within 5 minutes)
     */
    public boolean isAboutToExpire() {
        return LocalDateTime.now().plusMinutes(5).isAfter(this.expiresAt);
    }
}