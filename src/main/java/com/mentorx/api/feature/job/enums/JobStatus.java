package com.mentorx.api.feature.job.enums;

/**
 * Enum representing job posting status
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum JobStatus {
    /**
     * Job is in draft state
     */
    DRAFT,
    
    /**
     * Job is pending approval
     */
    PENDING_APPROVAL,
    
    /**
     * Job is published and accepting proposals
     */
    OPEN,
    
    /**
     * Job is in progress (contract signed)
     */
    IN_PROGRESS,
    
    /**
     * Job is completed
     */
    COMPLETED,
    
    /**
     * Job is cancelled
     */
    CANCELLED,
    
    /**
     * Job is closed (no longer accepting proposals)
     */
    CLOSED,
    
    /**
     * Job is on hold
     */
    ON_HOLD,
    
    /**
     * Job expired without being filled
     */
    EXPIRED
}