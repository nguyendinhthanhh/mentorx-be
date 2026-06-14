package com.mentorx.api.feature.complaint.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.util.SecurityUtils;
import com.mentorx.api.feature.complaint.dto.request.ComplaintCreateRequest;
import com.mentorx.api.feature.complaint.dto.request.ComplaintRespondRequest;
import com.mentorx.api.feature.complaint.dto.response.ComplaintEvidenceResponse;
import com.mentorx.api.feature.complaint.dto.response.ComplaintResponse;
import com.mentorx.api.feature.complaint.entity.Complaint;
import com.mentorx.api.feature.complaint.entity.ComplaintEvidence;
import com.mentorx.api.feature.complaint.enums.ComplaintStatus;
import com.mentorx.api.feature.complaint.repository.ComplaintEvidenceRepository;
import com.mentorx.api.feature.complaint.repository.ComplaintRepository;
import com.mentorx.api.feature.complaint.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintEvidenceRepository evidenceRepository;

    @Override
    @Transactional
    public ComplaintResponse createComplaint(ComplaintCreateRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (!currentUserId.equals(request.complainantId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Complainant ID must match authenticated user");
        }

        Complaint complaint = Complaint.builder()
            .complainantId(request.complainantId())
            .respondentId(request.respondentId())
            .sessionId(request.sessionId())
            .bookingId(request.bookingId())
            .title(request.title())
            .description(request.description())
            .complaintCategory(request.complaintCategory())
            .priorityLevel(request.priorityLevel() != null ? request.priorityLevel() : 3)
            .respondentNotifiedAt(LocalDateTime.now())
            .build();

        complaint.transitionStatus(ComplaintStatus.AWAITING_RESPONSE);
        complaint = complaintRepository.save(complaint);
        return toResponse(complaint);
    }

    @Override
    public ComplaintResponse getComplaintById(UUID complaintId) {
        Complaint complaint = findComplaint(complaintId);
        return toResponse(complaint);
    }

    @Override
    public Page<ComplaintResponse> getComplaintsByUser(UUID userId, Pageable pageable) {
        return complaintRepository.findByComplainantIdOrRespondentId(userId, userId, pageable)
            .map(this::toResponse);
    }

    @Override
    @Transactional
    public ComplaintResponse respondToComplaint(UUID complaintId, ComplaintRespondRequest request) {
        Complaint complaint = findComplaint(complaintId);
        complaint.setRespondentRespondedAt(LocalDateTime.now());
        complaint.setRespondentResponse(request.response());
        complaint.transitionStatus(ComplaintStatus.INVESTIGATING);
        complaint = complaintRepository.save(complaint);
        return toResponse(complaint);
    }

    @Override
    @Transactional
    public ComplaintResponse withdrawComplaint(UUID complaintId) {
        Complaint complaint = findComplaint(complaintId);
        complaint.withdraw();
        complaint = complaintRepository.save(complaint);
        return toResponse(complaint);
    }

    private Complaint findComplaint(UUID complaintId) {
        return complaintRepository.findById(complaintId)
            .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
    }

    private ComplaintResponse toResponse(Complaint complaint) {
        List<ComplaintEvidence> evidenceList = evidenceRepository.findByComplaintId(complaint.getId());
        List<ComplaintEvidenceResponse> evidenceResponses = evidenceList.stream()
            .map(this::toEvidenceResponse)
            .toList();

        return new ComplaintResponse(
            complaint.getId(),
            complaint.getComplainantId(),
            complaint.getRespondentId(),
            complaint.getSessionId(),
            complaint.getBookingId(),
            complaint.getTitle(),
            complaint.getDescription(),
            complaint.getComplaintCategory(),
            complaint.getStatus(),
            complaint.getPriorityLevel(),
            complaint.getMediatorId(),
            complaint.getMediatorAssignedAt(),
            complaint.getRespondentNotifiedAt(),
            complaint.getRespondentRespondedAt(),
            complaint.getRespondentResponse(),
            complaint.getResponseDeadline(),
            complaint.getMediationStartedAt(),
            complaint.getResolvedAt(),
            complaint.getOutcome(),
            complaint.getResolutionDetails(),
            complaint.getResolutionTimeHours(),
            complaint.getSlaMet(),
            evidenceResponses,
            complaint.getCreatedAt(),
            complaint.getUpdatedAt()
        );
    }

    private ComplaintEvidenceResponse toEvidenceResponse(ComplaintEvidence e) {
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
