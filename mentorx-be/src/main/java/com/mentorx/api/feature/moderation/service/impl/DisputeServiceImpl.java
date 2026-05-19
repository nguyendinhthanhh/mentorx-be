package com.mentorx.api.feature.moderation.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.moderation.dto.request.DisputeCreateRequest;
import com.mentorx.api.feature.moderation.dto.request.DisputeResolveRequest;
import com.mentorx.api.feature.moderation.dto.request.DisputeRespondRequest;
import com.mentorx.api.feature.moderation.dto.response.DisputeResponse;
import com.mentorx.api.feature.moderation.entity.Dispute;
import com.mentorx.api.feature.moderation.enums.DisputeStatus;
import com.mentorx.api.feature.moderation.repository.DisputeRepository;
import com.mentorx.api.feature.moderation.service.DisputeService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DisputeResponse createDispute(DisputeCreateRequest request) {
        User initiator = userRepository.findById(request.initiatorId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User respondent = userRepository.findById(request.respondentId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Dispute dispute = new Dispute();
        dispute.setInitiator(initiator);
        dispute.setRespondent(respondent);
        dispute.setContractId(request.contractId());
        dispute.setJobId(request.jobId());
        dispute.setTitle(request.title());
        dispute.setDescription(request.description());
        dispute.setDisputeCategory(request.disputeCategory());
        dispute.setDisputedAmountMxc(request.disputedAmountMxc());
        dispute.setRefundRequestedMxc(request.refundRequestedMxc());

        if (request.evidenceUrls() != null && !request.evidenceUrls().isEmpty()) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("initiatorEvidenceUrls", request.evidenceUrls());
            dispute.setMetadata(metadata);
            dispute.setInitiatorEvidenceCount(request.evidenceUrls().size());
        }

        return toResponse(disputeRepository.save(dispute));
    }

    @Override
    public DisputeResponse getDisputeById(UUID disputeId) {
        return toResponse(findDispute(disputeId));
    }

    @Override
    public Page<DisputeResponse> getDisputesByUser(UUID userId, Pageable pageable) {
        return disputeRepository.findByInitiatorIdOrRespondentId(userId, userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public DisputeResponse respondToDispute(UUID disputeId, DisputeRespondRequest request) {
        Dispute dispute = findDispute(disputeId);
        
        dispute.setRespondentResponse(request.response());
        dispute.setRespondentRespondedAt(LocalDateTime.now());
        
        if (request.evidenceUrls() != null && !request.evidenceUrls().isEmpty()) {
            Map<String, Object> metadata = dispute.getMetadata();
            if (metadata == null) metadata = new HashMap<>();
            metadata.put("respondentEvidenceUrls", request.evidenceUrls());
            dispute.setMetadata(metadata);
            dispute.setRespondentEvidenceCount(request.evidenceUrls().size());
        }

        dispute.setStatus(DisputeStatus.INVESTIGATING);

        return toResponse(disputeRepository.save(dispute));
    }

    @Override
    @Transactional
    public DisputeResponse assignMediator(UUID disputeId, UUID mediatorId) {
        Dispute dispute = findDispute(disputeId);
        User mediator = userRepository.findById(mediatorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        dispute.assignMediator(mediator);
        return toResponse(disputeRepository.save(dispute));
    }

    @Override
    @Transactional
    public DisputeResponse resolveDispute(UUID disputeId, DisputeResolveRequest request) {
        Dispute dispute = findDispute(disputeId);
        dispute.resolve(request.outcome(), request.resolutionDetails(), request.refundAmountMxc());
        return toResponse(disputeRepository.save(dispute));
    }

    private Dispute findDispute(UUID disputeId) {
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND)); 
    }

    @SuppressWarnings("unchecked")
    private DisputeResponse toResponse(Dispute dispute) {
        java.util.List<String> evidenceUrls = new java.util.ArrayList<>();
        if (dispute.getMetadata() != null) {
            if (dispute.getMetadata().containsKey("initiatorEvidenceUrls")) {
                evidenceUrls.addAll((java.util.List<String>) dispute.getMetadata().get("initiatorEvidenceUrls"));
            }
            if (dispute.getMetadata().containsKey("respondentEvidenceUrls")) {
                evidenceUrls.addAll((java.util.List<String>) dispute.getMetadata().get("respondentEvidenceUrls"));
            }
        }

        return new DisputeResponse(
                dispute.getId(),
                dispute.getInitiator().getId(),
                dispute.getInitiator().getFullName(),
                dispute.getRespondent().getId(),
                dispute.getRespondent().getFullName(),
                dispute.getContractId(),
                dispute.getJobId(),
                dispute.getTitle(),
                dispute.getDescription(),
                dispute.getDisputeCategory(),
                dispute.getStatus(),
                dispute.getPriorityLevel(),
                dispute.getDisputedAmountMxc(),
                dispute.getRefundRequestedMxc(),
                dispute.getMediator() != null ? dispute.getMediator().getId() : null,
                dispute.getMediatorAssignedAt(),
                dispute.getRespondentNotifiedAt(),
                dispute.getRespondentRespondedAt(),
                dispute.getRespondentResponse(),
                dispute.getResponseDeadline(),
                dispute.getMediationStartedAt(),
                dispute.getResolvedAt(),
                dispute.getOutcome(),
                dispute.getResolutionDetails(),
                dispute.getRefundAmountMxc(),
                dispute.getFundsInEscrow(),
                dispute.getEscrowRecordId(),
                evidenceUrls,
                dispute.getInitiatorEvidenceCount(),
                dispute.getRespondentEvidenceCount(),
                dispute.getRequiresArbitration(),
                dispute.getCreatedAt(),
                dispute.getUpdatedAt()
        );
    }
}
