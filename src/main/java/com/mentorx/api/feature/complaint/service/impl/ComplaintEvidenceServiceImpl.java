package com.mentorx.api.feature.complaint.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.complaint.dto.request.ComplaintEvidenceCreateRequest;
import com.mentorx.api.feature.complaint.dto.response.ComplaintEvidenceResponse;
import com.mentorx.api.feature.complaint.entity.Complaint;
import com.mentorx.api.feature.complaint.entity.ComplaintEvidence;
import com.mentorx.api.feature.complaint.repository.ComplaintEvidenceRepository;
import com.mentorx.api.feature.complaint.repository.ComplaintRepository;
import com.mentorx.api.feature.complaint.service.ComplaintEvidenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintEvidenceServiceImpl implements ComplaintEvidenceService {

    private final ComplaintEvidenceRepository evidenceRepository;
    private final ComplaintRepository complaintRepository;

    @Override
    @Transactional
    public ComplaintEvidenceResponse addEvidence(UUID complaintId, UUID userId, ComplaintEvidenceCreateRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));

        if (!complaint.getComplainantId().equals(userId) && !complaint.getRespondentId().equals(userId)) {
            throw new AppException(ErrorCode.NOT_DISPUTE_PARTY);
        }

        ComplaintEvidence evidence = ComplaintEvidence.builder()
            .complaintId(complaintId)
            .submittedByUserId(userId)
            .evidenceType(request.evidenceType())
            .title(request.title())
            .description(request.description())
            .fileUrl(request.fileUrl())
            .filename(request.filename())
            .mimeType(request.mimeType())
            .fileSize(request.fileSize())
            .build();

        evidence = evidenceRepository.save(evidence);
        return toResponse(evidence);
    }

    @Override
    public List<ComplaintEvidenceResponse> getEvidenceForComplaint(UUID complaintId) {
        return evidenceRepository.findByComplaintId(complaintId).stream()
            .map(this::toResponse)
            .toList();
    }

    private ComplaintEvidenceResponse toResponse(ComplaintEvidence e) {
        return new ComplaintEvidenceResponse(
            e.getId(), e.getComplaintId(), e.getSubmittedByUserId(),
            e.getEvidenceType(), e.getTitle(), e.getDescription(),
            e.getFileUrl(), e.getFilename(), e.getMimeType(), e.getFileSize(),
            e.getIsReviewed(), e.getReviewedAt(), e.getReviewedByUserId(),
            e.getReviewNotes(), e.getIsFlagged(), e.getFlagReason(),
            e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
