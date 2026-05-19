package com.mentorx.api.feature.job.enums;

/**
 * Enum representing milestone status
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum MilestoneStatus {
    /**
     * Milestone is pending start
     */
    PENDING,
    
    /**
     * Milestone is in progress
     */
    IN_PROGRESS,
    
    /**
     * Milestone is submitted for review
     */
    SUBMITTED,
    
    /**
     * Milestone is under review by client
     */
    UNDER_REVIEW,
    
    /**
     * Milestone requires revision
     */
    REVISION_REQUESTED,
    
    /**
     * Milestone is approved
     */
    APPROVED,
    
    /**
     * Milestone is rejected
     */
    REJECTED,
    
    /**
     * Milestone is completed and paid
     */
    COMPLETED,
    
    /**
     * Milestone is cancelled
     */
    CANCELLED,
    
    /**
     * Milestone is overdue
     */
    OVERDUE
}