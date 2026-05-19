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
 * Entity representing email verification tokens
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "email_verification_tokens", indexes = {
    @Index(name = "idx_email_token_user_id", columnList = "user_id"),
    @Index(name = "idx_email_token_token", columnList = "token"),
    @Index(name = "idx_email_token_email", columnList = "email"),
    @Index(name = "idx_email_token_expires", columnList = "expires_at"),
    @Index(name = "idx_email_token_used", columnList = "is_used")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Verification token
     */
    @NotNull
    @Size(max = 255)
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    /**
     * Email address to be verified
     */
    @NotNull
    @Size(max = 255)
    @Column(name = "email", nullable = false, length = 255)
    private String email;

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
     * IP address from which verification was requested
     */
    @Size(max = 45)
    @Column(name = "request_ip", length = 45)
    private String requestIp;

    /**
     * IP address from which verification was completed
     */
    @Size(max = 45)
    @Column(name = "verification_ip", length = 45)
    private String verificationIp;

    /**
     * User agent from verification request
     */
    @Size(max = 500)
    @Column(name = "request_user_agent", length = 500)
    private String requestUserAgent;

    /**
     * User agent from verification completion
     */
    @Size(max = 500)
    @Column(name = "verification_user_agent", length = 500)
    private String verificationUserAgent;

    /**
     * Number of verification attempts
     */
    @Column(name = "attempt_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer attemptCount = 0;

    /**
     * Last attempt timestamp
     */
    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    /**
     * Whether this is a resend token
     */
    @Column(name = "is_resend", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isResend = false;

    /**
     * Original token ID if this is a resend
     */
    @Column(name = "original_token_id")
    private UUID originalTokenId;

    @PrePersist
    protected void onCreate() {
        if (this.token == null) {
            this.token = UUID.randomUUID().toString();
        }
        
        if (this.expiresAt == null) {
            // Default expiration: 24 hours
            this.expiresAt = LocalDateTime.now().plusHours(24);
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
        return !this.isUsed && !isExpired();
    }

    /**
     * Marks the token as used
     */
    public void markAsUsed(String verificationIp, String verificationUserAgent) {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
        this.verificationIp = verificationIp;
        this.verificationUserAgent = verificationUserAgent;
    }

    /**
     * Records a verification attempt
     */
    public void recordAttempt() {
        this.attemptCount++;
        this.lastAttemptAt = LocalDateTime.now();
    }

    /**
     * Checks if maximum attempts have been exceeded
     */
    public boolean hasExceededMaxAttempts() {
        return this.attemptCount >= 5; // Maximum 5 attempts
    }

    /**
     * Generates a new token
     */
    public static EmailVerificationToken createToken(User user, String email) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setEmail(email);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        return token;
    }

    /**
     * Creates a resend token
     */
    public EmailVerificationToken createResendToken() {
        EmailVerificationToken resendToken = new EmailVerificationToken();
        resendToken.setUser(this.user);
        resendToken.setEmail(this.email);
        resendToken.setToken(UUID.randomUUID().toString());
        resendToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        resendToken.setIsResend(true);
        resendToken.setOriginalTokenId(this.getId());
        return resendToken;
    }
}