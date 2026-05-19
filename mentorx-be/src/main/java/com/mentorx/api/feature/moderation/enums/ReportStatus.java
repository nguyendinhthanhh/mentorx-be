package com.mentorx.api.feature.moderation.enums;

/**
 * Enum representing the status of a report
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum ReportStatus {
    /**
     * Report has been submitted and is waiting for review
     */
    PENDING,
    
    /**
     * Report is currently being reviewed by a moderator
     */
    UNDER_REVIEW,
    
    /**
     * Report has been escalated to senior moderator or admin
     */
    ESCALATED,
    
    /**
     * Report has been resolved with action taken
     */
    RESOLVED,
    
    /**
     * Report has been dismissed as invalid
     */
    DISMISSED,
    
    /**
     * Report is on hold pending additional information
     */
    ON_HOLD,
    
    /**
     * Report has been closed
     */
    CLOSED
}