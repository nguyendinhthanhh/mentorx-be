package com.mentorx.api.feature.job.enums;

/**
 * Enum representing negotiation status
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum NegotiationStatus {
    /**
     * Negotiation is pending response
     */
    PENDING,
    
    /**
     * Negotiation offer was accepted
     */
    ACCEPTED,
    
    /**
     * Negotiation offer was rejected
     */
    REJECTED,
    
    /**
     * A counter-offer was made in response
     */
    COUNTERED
}
