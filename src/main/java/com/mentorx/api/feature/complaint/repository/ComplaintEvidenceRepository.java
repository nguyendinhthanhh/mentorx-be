package com.mentorx.api.feature.complaint.repository;

import com.mentorx.api.feature.complaint.entity.ComplaintEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintEvidenceRepository extends JpaRepository<ComplaintEvidence, UUID> {
    List<ComplaintEvidence> findByDisputeId(UUID disputeId);
    List<ComplaintEvidence> findByDisputeIdAndSubmittedByUserId(UUID disputeId, UUID userId);
}
