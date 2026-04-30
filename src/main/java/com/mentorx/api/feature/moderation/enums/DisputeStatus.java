package com.mentorx.api.feature.moderation.enums;

/**
 * Enum representing the status of a dispute
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum DisputeStatus {
    /**
     * Dispute has been opened
     */
    OPEN,
    
    /**
     * Dispute is under investigation
     */
    INVESTIGATING,
    
    /**
     * Waiting for response from one party
     */
    AWAITING_RESPONSE,
    
    /**
     * Evidence is being reviewed
     */
    EVIDENCE_REVIEW,
    
    /**
     * Mediation is in progress
     */
    IN_MEDIATION,
    
    /**
     * Dispute has been escalated to arbitration
     */
    IN_ARBITRATION,
    
    /**
     * Dispute has been resolved
     */
    RESOLVED,
    
    /**
     * Dispute was closed without resolution
     */
    CLOSED,
    
    /**
     * Dispute was withdrawn by the initiator
     */
    WITHDRAWN,
    
    /**
     * Dispute expired without resolution
     */
    EXPIRED
}