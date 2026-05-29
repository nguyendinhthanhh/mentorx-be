package com.mentorx.api.feature.job.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.job.dto.request.NegotiationRequest;
import com.mentorx.api.feature.job.dto.response.NegotiationResponse;
import com.mentorx.api.feature.job.entity.Proposal;
import com.mentorx.api.feature.job.entity.ProposalNegotiation;
import com.mentorx.api.feature.job.enums.NegotiationStatus;
import com.mentorx.api.feature.job.enums.ProposalStatus;
import com.mentorx.api.feature.job.enums.SenderType;
import com.mentorx.api.feature.job.entity.Job;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.job.repository.ProposalNegotiationRepository;
import com.mentorx.api.feature.job.repository.ProposalRepository;
import com.mentorx.api.feature.job.service.ProposalNegotiationService;
import com.mentorx.api.feature.notification.dto.request.NotificationCreateRequest;
import com.mentorx.api.feature.notification.service.NotificationService;
import com.mentorx.api.feature.notification.enums.NotificationType;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProposalNegotiationServiceImpl implements ProposalNegotiationService {

    private final ProposalNegotiationRepository negotiationRepository;
    private final ProposalRepository proposalRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public NegotiationResponse clientCounterOffer(NegotiationRequest request) {
        Proposal proposal = findProposal(request.proposalId());
        User client = findUser(request.senderId());
        
        // Verify client owns the job
        if (!proposal.getJob().getClient().getId().equals(client.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_JOB_ACCESS);
        }
        
        // Mark previous pending negotiations as countered
        markPreviousAsCountered(request.proposalId());
        
        // Update proposal status to NEGOTIATING
        proposal.setStatus(ProposalStatus.NEGOTIATING);
        proposalRepository.save(proposal);

        // Update job activity
        Job job = proposal.getJob();
        job.setUpdatedAt(java.time.LocalDateTime.now());
        jobRepository.save(job);
        
        // Create negotiation
        ProposalNegotiation negotiation = createNegotiation(proposal, client, SenderType.CLIENT, request);
        NegotiationResponse response = toResponse(negotiationRepository.save(negotiation));
        
        // Send notification to mentor
        sendNotificationToMentor(proposal, client, request);
        
        return response;
    }

    @Override
    @Transactional
    public NegotiationResponse mentorCounterOffer(NegotiationRequest request) {
        Proposal proposal = findProposal(request.proposalId());
        User mentor = findUser(request.senderId());
        
        // Verify mentor owns the proposal
        if (!proposal.getMentor().getId().equals(mentor.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_JOB_ACCESS);
        }
        
        // Mark previous pending negotiations as countered
        markPreviousAsCountered(request.proposalId());
        
        // Update proposal status to NEGOTIATING
        proposal.setStatus(ProposalStatus.NEGOTIATING);
        proposalRepository.save(proposal);

        // Update job activity
        Job job = proposal.getJob();
        job.setUpdatedAt(java.time.LocalDateTime.now());
        jobRepository.save(job);
        
        // Create negotiation
        ProposalNegotiation negotiation = createNegotiation(proposal, mentor, SenderType.MENTOR, request);
        NegotiationResponse response = toResponse(negotiationRepository.save(negotiation));
        
        // Send notification to client
        sendNotificationToClient(proposal, mentor, request);
        
        return response;
    }

    @Override
    @Transactional
    public NegotiationResponse updatePendingNegotiation(UUID negotiationId, NegotiationRequest request) {
        ProposalNegotiation negotiation = findNegotiation(negotiationId);
        Proposal proposal = negotiation.getProposal();
        User sender = findUser(request.senderId());

        if (!proposal.getId().equals(request.proposalId())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        if (!negotiation.getSender().getId().equals(sender.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_JOB_ACCESS);
        }

        if (negotiation.getStatus() != NegotiationStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        negotiation.setMessage(request.message());
        negotiation.setProposedAmount(request.proposedAmount());
        negotiation.setProposedHourlyRate(request.proposedHourlyRate());
        negotiation.setEstimatedDurationDays(request.estimatedDurationDays());
        negotiation.setProposedStartDate(request.proposedStartDate());
        negotiation.setProposedDeliveryDate(request.proposedDeliveryDate());

        Job job = proposal.getJob();
        job.setUpdatedAt(java.time.LocalDateTime.now());
        jobRepository.save(job);

        return toResponse(negotiationRepository.save(negotiation));
    }

    @Override
    @Transactional
    public NegotiationResponse acceptNegotiation(UUID negotiationId, UUID userId) {
        ProposalNegotiation negotiation = findNegotiation(negotiationId);
        
        // Verify user is authorized (must be the receiver, not sender)
        Proposal proposal = negotiation.getProposal();
        boolean isClient = proposal.getJob().getClient().getId().equals(userId);
        boolean isMentor = proposal.getMentor().getId().equals(userId);
        
        if (!isClient && !isMentor) {
            throw new AppException(ErrorCode.UNAUTHORIZED_JOB_ACCESS);
        }
        
        // Verify user is not the sender
        if (negotiation.getSender().getId().equals(userId)) {
            throw new AppException(ErrorCode.BAD_REQUEST); // Cannot accept your own offer
        }
        
        // Accept negotiation
        negotiation.accept();
        
        // Update proposal with negotiated terms
        if (negotiation.getProposedAmount() != null) {
            proposal.setProposedAmount(negotiation.getProposedAmount());
        }
        if (negotiation.getProposedHourlyRate() != null) {
            proposal.setProposedHourlyRate(negotiation.getProposedHourlyRate());
        }
        if (negotiation.getEstimatedDurationDays() != null) {
            proposal.setEstimatedDurationDays(negotiation.getEstimatedDurationDays());
        }
        if (negotiation.getProposedStartDate() != null) {
            proposal.setProposedStartDate(negotiation.getProposedStartDate());
        }
        if (negotiation.getProposedDeliveryDate() != null) {
            proposal.setProposedDeliveryDate(negotiation.getProposedDeliveryDate());
        }
        
        // Update proposal status back to SUBMITTED (ready for final acceptance)
        proposal.setStatus(ProposalStatus.SUBMITTED);
        proposalRepository.save(proposal);

        // Update job activity
        Job job = proposal.getJob();
        job.setUpdatedAt(java.time.LocalDateTime.now());
        jobRepository.save(job);
        
        return toResponse(negotiationRepository.save(negotiation));
    }

    @Override
    @Transactional
    public void rejectNegotiation(UUID negotiationId, UUID userId) {
        ProposalNegotiation negotiation = findNegotiation(negotiationId);
        
        // Verify user is authorized
        Proposal proposal = negotiation.getProposal();
        boolean isClient = proposal.getJob().getClient().getId().equals(userId);
        boolean isMentor = proposal.getMentor().getId().equals(userId);
        
        if (!isClient && !isMentor) {
            throw new AppException(ErrorCode.UNAUTHORIZED_JOB_ACCESS);
        }
        
        // Verify user is not the sender
        if (negotiation.getSender().getId().equals(userId)) {
            throw new AppException(ErrorCode.BAD_REQUEST); // Cannot reject your own offer
        }
        
        negotiation.reject();
        negotiationRepository.save(negotiation);
    }

    @Override
    public List<NegotiationResponse> getByProposal(UUID proposalId) {
        return negotiationRepository.findByProposalIdOrderByCreatedAtAsc(proposalId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public NegotiationResponse getLatestByProposal(UUID proposalId) {
        return negotiationRepository.findLatestByProposalId(proposalId)
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.NEGOTIATION_NOT_FOUND));
    }

    private void markPreviousAsCountered(UUID proposalId) {
        List<ProposalNegotiation> pending = negotiationRepository
                .findByProposalIdAndStatus(proposalId, NegotiationStatus.PENDING);
        pending.forEach(ProposalNegotiation::counter);
        negotiationRepository.saveAll(pending);
    }

    private ProposalNegotiation createNegotiation(Proposal proposal, User sender, 
                                                   SenderType senderType, NegotiationRequest request) {
        ProposalNegotiation negotiation = new ProposalNegotiation();
        negotiation.setProposal(proposal);
        negotiation.setSender(sender);
        negotiation.setSenderType(senderType);
        negotiation.setMessage(request.message());
        negotiation.setProposedAmount(request.proposedAmount());
        negotiation.setProposedHourlyRate(request.proposedHourlyRate());
        negotiation.setEstimatedDurationDays(request.estimatedDurationDays());
        negotiation.setProposedStartDate(request.proposedStartDate());
        negotiation.setProposedDeliveryDate(request.proposedDeliveryDate());
        negotiation.setStatus(NegotiationStatus.PENDING);
        return negotiation;
    }

    private Proposal findProposal(UUID proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.PROPOSAL_NOT_FOUND));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private ProposalNegotiation findNegotiation(UUID negotiationId) {
        return negotiationRepository.findById(negotiationId)
                .orElseThrow(() -> new AppException(ErrorCode.NEGOTIATION_NOT_FOUND));
    }

    private NegotiationResponse toResponse(ProposalNegotiation negotiation) {
        return new NegotiationResponse(
                negotiation.getId(),
                negotiation.getProposal().getId(),
                negotiation.getSender().getId(),
                negotiation.getSender().getFullName(),
                negotiation.getSenderType(),
                negotiation.getMessage(),
                negotiation.getProposedAmount(),
                negotiation.getProposedHourlyRate(),
                negotiation.getEstimatedDurationDays(),
                negotiation.getProposedStartDate(),
                negotiation.getProposedDeliveryDate(),
                negotiation.getStatus(),
                negotiation.getCreatedAt(),
                negotiation.getRespondedAt()
        );
    }
    
    private void sendNotificationToMentor(Proposal proposal, User client, NegotiationRequest request) {
        try {
            String title = "💬 Đề xuất thương lượng mới";
            String message = String.format("%s đã gửi đề xuất thương lượng cho proposal của bạn: \"%s\"", 
                    client.getFullName(), 
                    request.message());
            
            NotificationCreateRequest notificationRequest = new NotificationCreateRequest(
                    proposal.getMentor().getId(),
                    NotificationType.NEW_MESSAGE,
                    title,
                    message,
                    proposal.getId(),
                    "PROPOSAL",
                    String.format("/mentor/proposals?proposalId=%s", proposal.getId()),
                    null,
                    1,
                    null,
                    "NEGOTIATION",
                    null,
                    client.getId()
            );
            
            notificationService.sendNotification(notificationRequest);
        } catch (Exception e) {
            // Log error but don't fail the negotiation
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }

    private void sendNotificationToClient(Proposal proposal, User mentor, NegotiationRequest request) {
        try {
            String title = "💬 Phản hồi thương lượng từ Mentor";
            String message = String.format("%s đã gửi phản hồi cho đề xuất thương lượng của bạn tại job: \"%s\"", 
                    mentor.getFullName(), 
                    proposal.getJob().getTitle());
            
            NotificationCreateRequest notificationRequest = new NotificationCreateRequest(
                    proposal.getJob().getClient().getId(),
                    NotificationType.NEW_MESSAGE,
                    title,
                    message,
                    proposal.getId(),
                    "PROPOSAL",
                    String.format("/jobs/%s?proposalId=%s", proposal.getJob().getId(), proposal.getId()),
                    null,
                    1,
                    null,
                    "NEGOTIATION",
                    null,
                    mentor.getId()
            );
            
            notificationService.sendNotification(notificationRequest);
        } catch (Exception e) {
            // Log error but don't fail the negotiation
            System.err.println("Failed to send notification to client: " + e.getMessage());
        }
    }
}
