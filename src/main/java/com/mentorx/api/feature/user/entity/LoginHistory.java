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
 * Entity representing user login history for security and analytics
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "login_history", indexes = {
    @Index(name = "idx_login_history_user_id", columnList = "user_id"),
    @Index(name = "idx_login_history_timestamp", columnList = "login_timestamp DESC"),
    @Index(name = "idx_login_history_user_time", columnList = "user_id, login_timestamp DESC"),
    @Index(name = "idx_login_history_ip", columnList = "ip_address"),
    @Index(name = "idx_login_history_status", columnList = "login_status"),
    @Index(name = "idx_login_history_device", columnList = "device_type")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistory extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * When the login occurred
     */
    @NotNull
    @Column(name = "login_timestamp", nullable = false)
    private LocalDateTime loginTimestamp;

    /**
     * Login status
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "login_status", nullable = false, length = 20)
    private String loginStatus; // SUCCESS, FAILED, BLOCKED, SUSPICIOUS

    /**
     * Login method
     */
    @Size(max = 30)
    @Column(name = "login_method", length = 30)
    private String loginMethod; // PASSWORD, OAUTH_GOOGLE, OAUTH_FACEBOOK, OAUTH_GITHUB, TWO_FACTOR, MAGIC_LINK, SSO

    /**
     * IP address
     */
    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent string
     */
    @Size(max = 500)
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Device type
     */
    @Size(max = 20)
    @Column(name = "device_type", length = 20)
    private String deviceType; // DESKTOP, MOBILE, TABLET, OTHER

    /**
     * Operating system
     */
    @Size(max = 50)
    @Column(name = "operating_system", length = 50)
    private String operatingSystem;

    /**
     * Browser name
     */
    @Size(max = 50)
    @Column(name = "browser", length = 50)
    private String browser;

    /**
     * Browser version
     */
    @Size(max = 20)
    @Column(name = "browser_version", length = 20)
    private String browserVersion;

    /**
     * Country code (from IP geolocation)
     */
    @Size(max = 10)
    @Column(name = "country_code", length = 10)
    private String countryCode;

    /**
     * City (from IP geolocation)
     */
    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    /**
     * Region/state (from IP geolocation)
     */
    @Size(max = 100)
    @Column(name = "region", length = 100)
    private String region;

    /**
     * Latitude
     */
    @Column(name = "latitude", precision = 10, scale = 7)
    private java.math.BigDecimal latitude;

    /**
     * Longitude
     */
    @Column(name = "longitude", precision = 10, scale = 7)
    private java.math.BigDecimal longitude;

    /**
     * Session ID
     */
    @Size(max = 255)
    @Column(name = "session_id", length = 255)
    private String sessionId;

    /**
     * When the session ended
     */
    @Column(name = "logout_timestamp")
    private LocalDateTime logoutTimestamp;

    /**
     * Session duration in minutes
     */
    @Column(name = "session_duration_minutes")
    private Integer sessionDurationMinutes;

    /**
     * Whether this login was from a new device
     */
    @Column(name = "is_new_device", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isNewDevice = false;

    /**
     * Whether this login was from a new location
     */
    @Column(name = "is_new_location", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isNewLocation = false;

    /**
     * Whether this login was flagged as suspicious
     */
    @Column(name = "is_suspicious", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isSuspicious = false;

    /**
     * Reason for suspicion
     */
    @Size(max = 200)
    @Column(name = "suspicion_reason", length = 200)
    private String suspicionReason;

    /**
     * Risk score (0-100, higher = more risky)
     */
    @Column(name = "risk_score")
    private Integer riskScore;

    /**
     * Whether two-factor authentication was used
     */
    @Column(name = "two_factor_used", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean twoFactorUsed = false;

    /**
     * Two-factor method
     */
    @Size(max = 30)
    @Column(name = "two_factor_method", length = 30)
    private String twoFactorMethod; // SMS, EMAIL, AUTHENTICATOR_APP, BACKUP_CODE

    /**
     * Failure reason (if login failed)
     */
    @Size(max = 200)
    @Column(name = "failure_reason", length = 200)
    private String failureReason;

    /**
     * Number of failed attempts before this login
     */
    @Column(name = "failed_attempts_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer failedAttemptsCount = 0;

    /**
     * Referrer URL
     */
    @Size(max = 500)
    @Column(name = "referrer_url", length = 500)
    private String referrerUrl;

    /**
     * Additional metadata as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Whether user was notified about this login
     */
    @Column(name = "user_notified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean userNotified = false;

    /**
     * When user was notified
     */
    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    /**
     * Whether this login was verified by user
     */
    @Column(name = "is_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isVerified = false;

    /**
     * When user verified this login
     */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    /**
     * ISP/Organization
     */
    @Size(max = 200)
    @Column(name = "isp", length = 200)
    private String isp;

    /**
     * Timezone
     */
    @Size(max = 50)
    @Column(name = "timezone", length = 50)
    private String timezone;

    @PrePersist
    protected void onCreate() {
        if (this.loginTimestamp == null) {
            this.loginTimestamp = LocalDateTime.now();
        }
    }

    /**
     * Checks if the login was successful
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(this.loginStatus);
    }

    /**
     * Checks if the login was failed
     */
    public boolean isFailed() {
        return "FAILED".equals(this.loginStatus);
    }

    /**
     * Checks if the login was blocked
     */
    public boolean isBlocked() {
        return "BLOCKED".equals(this.loginStatus);
    }

    /**
     * Records logout
     */
    public void recordLogout() {
        this.logoutTimestamp = LocalDateTime.now();
        
        if (this.loginTimestamp != null) {
            long minutes = java.time.Duration.between(this.loginTimestamp, this.logoutTimestamp).toMinutes();
            this.sessionDurationMinutes = (int) minutes;
        }
    }

    /**
     * Marks as suspicious
     */
    public void markAsSuspicious(String reason, Integer riskScore) {
        this.isSuspicious = true;
        this.suspicionReason = reason;
        this.riskScore = riskScore;
    }

    /**
     * Marks as verified by user
     */
    public void markAsVerified() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * Records user notification
     */
    public void recordNotification() {
        this.userNotified = true;
        this.notifiedAt = LocalDateTime.now();
    }

    /**
     * Checks if this is a high-risk login
     */
    public boolean isHighRisk() {
        return this.riskScore != null && this.riskScore >= 70;
    }

    /**
     * Gets location string
     */
    public String getLocationString() {
        StringBuilder location = new StringBuilder();
        
        if (city != null) {
            location.append(city);
        }
        
        if (region != null) {
            if (location.length() > 0) location.append(", ");
            location.append(region);
        }
        
        if (countryCode != null) {
            if (location.length() > 0) location.append(", ");
            location.append(countryCode);
        }
        
        return location.length() > 0 ? location.toString() : "Unknown";
    }
}