package com.mentorx.api.feature.job.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.feature.job.dto.request.NegotiationRequest;
import com.mentorx.api.feature.job.dto.response.NegotiationResponse;
import com.mentorx.api.feature.job.entity.Contract;
import com.mentorx.api.feature.job.entity.Proposal;
import com.mentorx.api.feature.job.entity.ProposalNegotiation;
import com.mentorx.api.feature.job.entity.Job;
import com.mentorx.api.feature.job.enums.ContractStatus;
import com.mentorx.api.feature.job.enums.NegotiationStatus;
import com.mentorx.api.feature.job.enums.ProposalStatus;
import com.mentorx.api.feature.job.enums.SenderType;
import com.mentorx.api.feature.job.repository.ContractRepository;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.job.repository.ProposalNegotiationRepository;
import com.mentorx.api.feature.job.repository.ProposalRepository;
import com.mentorx.api.feature.job.service.ProposalNegotiationService;
import com.mentorx.api.feature.notification.dto.request.NotificationCreateRequest;
import com.mentorx.api.feature.notification.service.NotificationService;
import com.mentorx.api.feature.notification.enums.NotificationType;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProposalNegotiationServiceImpl implements ProposalNegotiationService {

    private final ProposalNegotiationRepository negotiationRepository;
    private final ProposalRepository proposalRepository;
    private final ContractRepository contractRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MentorModeAccessService mentorModeAccessService;
    private final WalletService walletService;

    @Override
    @Transactional
    public NegotiationResponse clientCounterOffer(NegotiationRequest request) {
        Proposal proposal = findProposal(request.proposalId());
        User client = findUser(request.senderId());
        requireCurrentUser(request.senderId());
        
        // Verify client owns the job
        if (!proposal.getJob().getClient().getId().equals(client.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_JOB_ACCESS);
        }
        ensureNegotiationAllowed(proposal);
        validateNegotiationTerms(request);
        
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
        requireCurrentUser(request.senderId());
        
        // Verify mentor owns the proposal
        if (!proposal.getMentor().getId().equals(mentor.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_JOB_ACCESS);
        }
        ensureNegotiationAllowed(proposal);
        validateNegotiationTerms(request);
        
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
        requireCurrentUser(request.senderId());

        if (!proposal.getId().equals(request.proposalId())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        if (!negotiation.getSender().getId().equals(sender.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_JOB_ACCESS);
        }

        if (negotiation.getStatus() != NegotiationStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        ensureNegotiationAllowed(proposal);
        throw new AppException(ErrorCode.BAD_REQUEST, "Pending negotiation offers cannot be edited after sending.");
    }

    @Override
    @Transactional
    public NegotiationResponse acceptNegotiation(UUID negotiationId, UUID userId) {
        ProposalNegotiation negotiation = findNegotiation(negotiationId);
        requireCurrentUser(userId);

        Proposal proposal = proposalRepository.findByIdForUpdate(negotiation.getProposal().getId())
                .orElseThrow(() -> new AppException(ErrorCode.PROPOSAL_NOT_FOUND));
        ensureNegotiationAllowed(proposal);

        boolean isClient = proposal.getJob().getClient().getId().equals(userId);
        boolean isMentor = proposal.getMentor().getId().equals(userId);
        if (!isClient && !isMentor) {
            throw new AppException(ErrorCode.UNAUTHORIZED_JOB_ACCESS);
        }
        if (negotiation.getSender().getId().equals(userId)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Cannot accept your own offer.");
        }

        Job job = proposal.getJob();
        if (contractRepository.findByProposalId(proposal.getId()).isPresent()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "A contract already exists for this proposal.");
        }

        BigDecimal finalAmount = Optional.ofNullable(negotiation.getProposedAmount())
                .filter(amount -> amount.compareTo(BigDecimal.ZERO) > 0)
                .orElseGet(proposal::getProposedAmount);
        if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Price must be greater than 0 MXC.");
        }

        Integer finalDurationDays = Optional.ofNullable(negotiation.getEstimatedDurationDays())
                .orElse(proposal.getEstimatedDurationDays());
        LocalDateTime finalDeadlineAt = Optional.ofNullable(negotiation.getDeadlineAt())
                .orElse(proposal.getDeadlineAt());
        String finalScopeDescription = Optional.ofNullable(negotiation.getMessage())
                .filter(message -> !message.isBlank())
                .orElseGet(() -> proposal.getCoverLetter() != null && !proposal.getCoverLetter().isBlank()
                        ? proposal.getCoverLetter()
                        : proposal.getScopeDescription());

        if (walletService.getUserAvailableBalance(job.getClient().getId()).compareTo(finalAmount) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        negotiation.accept();
        proposal.setProposedAmount(finalAmount);
        proposal.setEstimatedDurationDays(finalDurationDays);
        proposal.setDeadlineAt(finalDeadlineAt);
        proposal.setScopeDescription(finalScopeDescription == null ? null : finalScopeDescription.trim());
        if (negotiation.getProposedHourlyRate() != null) {
            proposal.setProposedHourlyRate(negotiation.getProposedHourlyRate());
        }
        if (negotiation.getProposedStartDate() != null) {
            proposal.setProposedStartDate(negotiation.getProposedStartDate());
        }
        if (negotiation.getProposedDeliveryDate() != null) {
            proposal.setProposedDeliveryDate(negotiation.getProposedDeliveryDate());
        }
        proposal.accept();

        Contract contract = buildAcceptedContract(proposal, finalAmount, finalDurationDays, finalDeadlineAt, finalScopeDescription);
        Contract savedContract = contractRepository.save(contract);
        walletService.processJobPayment(job.getClient().getId(), savedContract.getId(), finalAmount);
        savedContract.setAmountInEscrow(finalAmount);
        savedContract.setFundsInEscrow(true);
        savedContract.setStatus(ContractStatus.ACTIVE);
        savedContract.setActivatedAt(LocalDateTime.now());
        contractRepository.save(savedContract);

        proposalRepository.save(proposal);
        negotiationRepository.save(negotiation);

        job.setUpdatedAt(java.time.LocalDateTime.now());
        job.setStatus(JobStatus.IN_PROGRESS);
        jobRepository.save(job);

        autoCloseOtherProposals(job.getId(), proposal.getId());

        return toResponse(negotiation);
    }

    @Override
    @Transactional
    public void rejectNegotiation(UUID negotiationId, UUID userId) {
        ProposalNegotiation negotiation = findNegotiation(negotiationId);
        requireCurrentUser(userId);
        
        // Verify user is authorized
        Proposal proposal = negotiation.getProposal();
        ensureNegotiationAllowed(proposal);
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

        if (negotiationRepository.findByProposalIdAndStatus(proposal.getId(), NegotiationStatus.PENDING).isEmpty()) {
            proposal.setStatus(ProposalStatus.SUBMITTED);
            proposalRepository.save(proposal);
        }
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
        negotiation.setDeadlineAt(request.deadlineAt());
        negotiation.setScopeDescription(request.scopeDescription() == null ? null : request.scopeDescription().trim());
        negotiation.setProposedStartDate(request.proposedStartDate());
        negotiation.setProposedDeliveryDate(request.proposedDeliveryDate());
        negotiation.setStatus(NegotiationStatus.PENDING);
        return negotiation;
    }

    private void validateNegotiationTerms(NegotiationRequest request) {
        if (request.proposedAmount() == null || request.proposedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Price must be greater than 0 MXC.");
        }

        if (request.deadlineAt() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Deadline is required.");
        }

        if (!request.deadlineAt().isAfter(LocalDateTime.now())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Deadline must be in the future.");
        }

        String message = request.message() == null ? "" : request.message().trim();
        if (message.length() < 20 || message.length() > 1000) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Message must be between 20 and 1000 characters.");
        }
    }

    private Proposal findProposal(UUID proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.PROPOSAL_NOT_FOUND));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private void requireCurrentUser(UUID userId) {
        if (!mentorModeAccessService.getCurrentUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "You cannot negotiate on behalf of another user.");
        }
    }

    private void ensureNegotiationAllowed(Proposal proposal) {
        Job job = proposal.getJob();
        if (job.getStatus() != JobStatus.OPEN) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Negotiation is only available while the job is open.");
        }
        if (contractRepository.existsByJobIdAndStatusIn(job.getId(), java.util.List.of(
                ContractStatus.ACTIVE,
                ContractStatus.PENDING_PAYMENT,
                ContractStatus.PAUSED,
                ContractStatus.IN_DISPUTE,
                ContractStatus.UNDER_REVIEW
        ))) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Negotiation is no longer available because this job already has an active contract.");
        }
        if (!(proposal.getStatus() == ProposalStatus.SUBMITTED
                || proposal.getStatus() == ProposalStatus.NEGOTIATING
                || proposal.getStatus() == ProposalStatus.OFFER_ACCEPTED
                || proposal.getStatus() == ProposalStatus.SHORTLISTED
                || proposal.getStatus() == ProposalStatus.UNDER_REVIEW)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "This proposal is not in a negotiable state.");
        }
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
                negotiation.getDeadlineAt(),
                negotiation.getScopeDescription(),
                negotiation.getProposedStartDate(),
                negotiation.getProposedDeliveryDate(),
                negotiation.getStatus(),
                negotiation.getCreatedAt(),
                negotiation.getRespondedAt()
        );
    }

    private Contract buildAcceptedContract(
            Proposal proposal,
            BigDecimal finalAmount,
            Integer finalDurationDays,
            LocalDateTime finalDeadlineAt,
            String finalScopeDescription
    ) {
        Contract contract = new Contract();
        Job proposalJob = proposal.getJob();
        LocalDate today = LocalDate.now();
        String normalizedScope = finalScopeDescription == null || finalScopeDescription.isBlank()
                ? proposalJob.getSuccessCriteria()
                : finalScopeDescription.trim();

        contract.setJob(proposalJob);
        contract.setProposal(proposal);
        contract.setClient(proposalJob.getClient());
        contract.setMentor(proposal.getMentor());
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setTitle(proposalJob.getTitle());
        contract.setDescription(proposalJob.getDescription());
        contract.setTotalAmount(finalAmount);
        contract.setHourlyRate(proposal.getProposedHourlyRate());
        contract.setStartDate(proposal.getProposedStartDate() != null ? proposal.getProposedStartDate() : today);
        contract.setEndDate(resolveContractEndDate(proposal, finalDurationDays, today));
        contract.setDeadlineAt(finalDeadlineAt);
        contract.setScopeDescription(normalizedScope);
        contract.setActualStartDate(today);
        contract.setActivatedAt(LocalDateTime.now());
        contract.setPaymentTerms("Funds are held in escrow until the client confirms the job is completed.");
        contract.setDeliverables(normalizedScope);
        contract.setAmountPaid(BigDecimal.ZERO);
        contract.setAmountInEscrow(finalAmount);
        contract.setFundsInEscrow(true);
        return contract;
    }

    private LocalDate resolveContractEndDate(Proposal proposal, Integer finalDurationDays, LocalDate fallbackStartDate) {
        if (proposal.getProposedDeliveryDate() != null) {
            return proposal.getProposedDeliveryDate();
        }
        if (finalDurationDays == null || finalDurationDays <= 0) {
            return null;
        }
        LocalDate startDate = proposal.getProposedStartDate() != null ? proposal.getProposedStartDate() : fallbackStartDate;
        return startDate.plusDays(finalDurationDays.longValue());
    }

    private void autoCloseOtherProposals(UUID jobId, UUID acceptedProposalId) {
        List<Proposal> siblingProposals = proposalRepository.findByJobId(jobId);
        for (Proposal sibling : siblingProposals) {
            if (sibling.getId().equals(acceptedProposalId)) {
                continue;
            }

            if (sibling.getStatus() == ProposalStatus.SUBMITTED
                    || sibling.getStatus() == ProposalStatus.NEGOTIATING
                    || sibling.getStatus() == ProposalStatus.OFFER_ACCEPTED
                    || sibling.getStatus() == ProposalStatus.SHORTLISTED
                    || sibling.getStatus() == ProposalStatus.UNDER_REVIEW) {
                sibling.autoClose("Another mentor was selected for this job.");
                proposalRepository.save(sibling);
            }

            List<ProposalNegotiation> pendingNegotiations = negotiationRepository.findByProposalIdAndStatus(
                    sibling.getId(),
                    NegotiationStatus.PENDING
            );
            if (!pendingNegotiations.isEmpty()) {
                pendingNegotiations.forEach(ProposalNegotiation::cancel);
                negotiationRepository.saveAll(pendingNegotiations);
            }
        }
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
