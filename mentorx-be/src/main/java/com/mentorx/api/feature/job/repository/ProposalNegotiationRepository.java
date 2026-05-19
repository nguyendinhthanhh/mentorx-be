package com.mentorx.api.feature.job.repository;

import com.mentorx.api.feature.job.entity.ProposalNegotiation;
import com.mentorx.api.feature.job.enums.NegotiationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProposalNegotiationRepository extends JpaRepository<ProposalNegotiation, UUID> {
    
    /**
     * Find all negotiations for a proposal, ordered by creation date
     */
    @Query("SELECT n FROM ProposalNegotiation n WHERE n.proposal.id = :proposalId ORDER BY n.createdAt ASC")
    List<ProposalNegotiation> findByProposalIdOrderByCreatedAtAsc(@Param("proposalId") UUID proposalId);
    
    /**
     * Find the latest negotiation for a proposal
     */
    @Query("SELECT n FROM ProposalNegotiation n WHERE n.proposal.id = :proposalId ORDER BY n.createdAt DESC LIMIT 1")
    Optional<ProposalNegotiation> findLatestByProposalId(@Param("proposalId") UUID proposalId);
    
    /**
     * Find pending negotiations for a proposal
     */
    List<ProposalNegotiation> findByProposalIdAndStatus(UUID proposalId, NegotiationStatus status);
    
    /**
     * Count negotiations for a proposal
     */
    long countByProposalId(UUID proposalId);
}
