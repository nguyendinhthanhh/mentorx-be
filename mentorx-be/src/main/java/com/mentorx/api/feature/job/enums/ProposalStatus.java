package com.mentorx.api.feature.job.enums;

/**
 * Enum representing proposal status
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum ProposalStatus {
    /**
     * Proposal is in draft state
     */
    DRAFT,
    
    /**
     * Proposal has been submitted
     */
    SUBMITTED,
    
    /**
     * Proposal is under review by client
     */
    UNDER_REVIEW,
    
    /**
     * Client has shortlisted this proposal
     */
    SHORTLISTED,
    
    /**
     * Proposal has been accepted
     */
    ACCEPTED,
    
    /**
     * Proposal has been rejected
     */
    REJECTED,
    
    /**
     * Proposal has been withdrawn by mentor
     */
    WITHDRAWN,
    
    /**
     * Proposal expired (job closed)
     */
    EXPIRED,
    
    /**
     * Interview requested
     */
    INTERVIEW_REQUESTED,
    
    /**
     * Negotiation in progress
     */
    NEGOTIATING
}