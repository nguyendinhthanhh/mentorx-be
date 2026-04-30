package com.mentorx.api.feature.user.enums;

/**
 * Enum representing user account status
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum UserStatus {
    /**
     * Account is active and in good standing
     */
    ACTIVE,
    
    /**
     * Account is pending email verification
     */
    PENDING_VERIFICATION,
    
    /**
     * Account is temporarily suspended
     */
    SUSPENDED,
    
    /**
     * Account is permanently banned
     */
    BANNED,
    
    /**
     * Account is inactive (user hasn't logged in for a long time)
     */
    INACTIVE,
    
    /**
     * Account is deactivated by user
     */
    DEACTIVATED,
    
    /**
     * Account is deleted (soft delete)
     */
    DELETED,
    
    /**
     * Account is locked due to security reasons
     */
    LOCKED
}