package com.mentorx.api.feature.user.enums;

/**
 * Enum representing mentor profile status
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum MentorStatus {
    /**
     * Mentor profile is pending approval
     */
    PENDING_APPROVAL,
    
    /**
     * Mentor profile is under review
     */
    UNDER_REVIEW,
    
    /**
     * Mentor profile is approved and active
     */
    APPROVED,
    
    /**
     * Mentor profile is rejected
     */
    REJECTED,
    
    /**
     * Mentor is temporarily unavailable
     */
    UNAVAILABLE,
    
    /**
     * Mentor profile is suspended
     */
    SUSPENDED,
    
    /**
     * Mentor profile is deactivated
     */
    DEACTIVATED,
    
    /**
     * Mentor is on vacation/break
     */
    ON_BREAK
}