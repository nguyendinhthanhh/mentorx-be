package com.mentorx.api.feature.job.enums;

/**
 * Enum representing contract status
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum ContractStatus {
    /**
     * Contract is being drafted
     */
    DRAFT,
    
    /**
     * Contract is pending signatures
     */
    PENDING_SIGNATURE,
    
    /**
     * Contract is active and in progress
     */
    ACTIVE,
    
    /**
     * Contract is paused
     */
    PAUSED,
    
    /**
     * Contract is completed successfully
     */
    COMPLETED,
    
    /**
     * Contract is cancelled
     */
    CANCELLED,
    
    /**
     * Contract is terminated
     */
    TERMINATED,
    
    /**
     * Contract is in dispute
     */
    IN_DISPUTE,
    
    /**
     * Contract expired
     */
    EXPIRED,
    
    /**
     * Contract is pending payment
     */
    PENDING_PAYMENT,
    
    /**
     * Contract is under review
     */
    UNDER_REVIEW
}