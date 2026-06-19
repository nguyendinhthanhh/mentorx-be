package com.mentorx.api.feature.complaint.service;

import com.mentorx.api.feature.complaint.dto.request.ComplaintEvidenceCreateRequest;
import com.mentorx.api.feature.complaint.dto.response.ComplaintEvidenceResponse;

import java.util.List;
import java.util.UUID;

public interface ComplaintEvidenceService {
    ComplaintEvidenceResponse addEvidence(UUID complaintId, UUID userId, ComplaintEvidenceCreateRequest request);
    List<ComplaintEvidenceResponse> getEvidenceForComplaint(UUID complaintId);
}
