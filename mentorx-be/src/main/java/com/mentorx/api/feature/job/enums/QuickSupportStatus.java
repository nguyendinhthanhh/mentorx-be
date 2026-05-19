package com.mentorx.api.feature.job.enums;

/**
 * Enum representing quick support request status
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum QuickSupportStatus {
    /**
     * Request is pending matching
     */
    PENDING,
    
    /**
     * Mentor has been matched
     */
    MATCHED,
    
    /**
     * Session is in progress
     */
    IN_PROGRESS,
    
    /**
     * Session is completed
     */
    COMPLETED,
    
    /**
     * Request was cancelled by client
     */
    CANCELLED,
    
    /**
     * Request expired without match
     */
    EXPIRED,
    
    /**
     * Mentor declined the request
     */
    DECLINED,
    
    /**
     * No mentor available
     */
    NO_MENTOR_AVAILABLE
}