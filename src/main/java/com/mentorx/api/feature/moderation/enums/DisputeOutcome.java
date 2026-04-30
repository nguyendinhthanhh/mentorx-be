package com.mentorx.api.feature.moderation.enums;

/**
 * Enum representing the outcome of a resolved dispute
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum DisputeOutcome {
    /**
     * Dispute was resolved in favor of the initiator
     */
    FAVOR_INITIATOR,
    
    /**
     * Dispute was resolved in favor of the respondent
     */
    FAVOR_RESPONDENT,
    
    /**
     * Dispute was resolved with a compromise/split decision
     */
    COMPROMISE,
    
    /**
     * Full refund was issued
     */
    FULL_REFUND,
    
    /**
     * Partial refund was issued
     */
    PARTIAL_REFUND,
    
    /**
     * No refund, work accepted as is
     */
    NO_REFUND,
    
    /**
     * Contract was cancelled
     */
    CONTRACT_CANCELLED,
    
    /**
     * Additional work was required
     */
    ADDITIONAL_WORK_REQUIRED,
    
    /**
     * Parties reached mutual agreement
     */
    MUTUAL_AGREEMENT,
    
    /**
     * Dispute was deemed invalid
     */
    INVALID_DISPUTE,
    
    /**
     * No outcome determined
     */
    NO_OUTCOME
}