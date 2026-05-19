package com.mentorx.api.feature.user.entity;

import com.mentorx.api.common.entity.BaseEntity;
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
 * Entity representing OAuth provider connections for users
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_oauth_providers", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "provider_name"}),
       indexes = {
    @Index(name = "idx_oauth_user_id", columnList = "user_id"),
    @Index(name = "idx_oauth_provider", columnList = "provider_name"),
    @Index(name = "idx_oauth_provider_user_id", columnList = "provider_name, provider_user_id"),
    @Index(name = "idx_oauth_active", columnList = "is_active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserOAuthProvider extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * OAuth provider name (google, facebook, github, linkedin, etc.)
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;

    /**
     * User ID from the OAuth provider
     */
    @NotNull
    @Size(max = 255)
    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    /**
     * Email from the OAuth provider
     */
    @Size(max = 255)
    @Column(name = "provider_email", length = 255)
    private String providerEmail;

    /**
     * Display name from the OAuth provider
     */
    @Size(max = 255)
    @Column(name = "provider_display_name", length = 255)
    private String providerDisplayName;

    /**
     * Profile picture URL from the OAuth provider
     */
    @Size(max = 500)
    @Column(name = "provider_picture_url", length = 500)
    private String providerPictureUrl;

    /**
     * Access token (encrypted)
     */
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    /**
     * Refresh token (encrypted)
     */
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    /**
     * Token expiration time
     */
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    /**
     * Scopes granted by the user
     */
    @Size(max = 500)
    @Column(name = "granted_scopes", length = 500)
    private String grantedScopes;

    /**
     * Whether this OAuth connection is active
     */
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    /**
     * Whether this is the primary OAuth provider
     */
    @Column(name = "is_primary", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPrimary = false;

    /**
     * When the OAuth connection was first established
     */
    @NotNull
    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;

    /**
     * When the OAuth connection was last used
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * When the token was last refreshed
     */
    @Column(name = "last_token_refresh_at")
    private LocalDateTime lastTokenRefreshAt;

    /**
     * Additional provider data as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provider_data", columnDefinition = "jsonb")
    private Map<String, Object> providerData;

    /**
     * Number of times this connection has been used
     */
    @Column(name = "usage_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer usageCount = 0;

    /**
     * When the connection was disconnected (if applicable)
     */
    @Column(name = "disconnected_at")
    private LocalDateTime disconnectedAt;

    /**
     * Reason for disconnection
     */
    @Size(max = 200)
    @Column(name = "disconnect_reason", length = 200)
    private String disconnectReason;

    /**
     * Whether email is verified by the provider
     */
    @Column(name = "email_verified_by_provider", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean emailVerifiedByProvider = false;

    /**
     * Locale/language from the provider
     */
    @Size(max = 10)
    @Column(name = "provider_locale", length = 10)
    private String providerLocale;

    @PrePersist
    protected void onCreate() {
        if (this.connectedAt == null) {
            this.connectedAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if the token has expired
     */
    public boolean isTokenExpired() {
        return this.tokenExpiresAt != null && LocalDateTime.now().isAfter(this.tokenExpiresAt);
    }

    /**
     * Checks if the token needs refresh (expires within 5 minutes)
     */
    public boolean needsTokenRefresh() {
        return this.tokenExpiresAt != null && 
               LocalDateTime.now().plusMinutes(5).isAfter(this.tokenExpiresAt);
    }

    /**
     * Updates the access token
     */
    public void updateToken(String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this.accessToken = accessToken;
        if (refreshToken != null) {
            this.refreshToken = refreshToken;
        }
        this.tokenExpiresAt = expiresAt;
        this.lastTokenRefreshAt = LocalDateTime.now();
    }

    /**
     * Records usage of this OAuth connection
     */
    public void recordUsage() {
        this.lastUsedAt = LocalDateTime.now();
        this.usageCount++;
    }

    /**
     * Disconnects the OAuth provider
     */
    public void disconnect(String reason) {
        this.isActive = false;
        this.disconnectedAt = LocalDateTime.now();
        this.disconnectReason = reason;
        // Clear sensitive data
        this.accessToken = null;
        this.refreshToken = null;
    }
}