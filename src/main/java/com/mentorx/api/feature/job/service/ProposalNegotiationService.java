package com.mentorx.api.feature.job.service;

import com.mentorx.api.feature.job.dto.request.NegotiationRequest;
import com.mentorx.api.feature.job.dto.response.NegotiationResponse;

import java.util.List;
import java.util.UUID;

public interface ProposalNegotiationService {
    /**
     * Client sends counter-offer to mentor
     */
    NegotiationResponse clientCounterOffer(NegotiationRequest request);
    
    /**
     * Mentor responds with counter-offer to client
     */
    NegotiationResponse mentorCounterOffer(NegotiationRequest request);

    /**
     * Update a pending negotiation created by the same sender
     */
    NegotiationResponse updatePendingNegotiation(UUID negotiationId, NegotiationRequest request);
    
    /**
     * Accept a negotiation offer
     */
    NegotiationResponse acceptNegotiation(UUID negotiationId, UUID userId);
    
    /**
     * Reject a negotiation offer
     */
    void rejectNegotiation(UUID negotiationId, UUID userId);
    
    /**
     * Get all negotiations for a proposal
     */
    List<NegotiationResponse> getByProposal(UUID proposalId);
    
    /**
     * Get latest negotiation for a proposal
     */
    NegotiationResponse getLatestByProposal(UUID proposalId);
}
