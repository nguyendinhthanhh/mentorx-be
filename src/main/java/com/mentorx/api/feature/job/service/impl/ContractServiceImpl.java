package com.mentorx.api.feature.job.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.feature.job.dto.request.ContractCancellationDecisionRequest;
import com.mentorx.api.feature.job.dto.request.ContractCancellationRequest;
import com.mentorx.api.feature.job.dto.request.ContractCreateRequest;
import com.mentorx.api.feature.job.dto.response.ContractResponse;
import com.mentorx.api.feature.job.entity.Contract;
import com.mentorx.api.feature.job.entity.Job;
import com.mentorx.api.feature.job.entity.Proposal;
import com.mentorx.api.feature.job.enums.ContractStatus;
import com.mentorx.api.feature.job.repository.ContractRepository;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.job.repository.ProposalRepository;
import com.mentorx.api.feature.job.service.ContractService;
import com.mentorx.api.feature.notification.dto.request.NotificationCreateRequest;
import com.mentorx.api.feature.notification.enums.NotificationType;
import com.mentorx.api.feature.notification.service.NotificationService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final NotificationService notificationService;
    private final MentorModeAccessService mentorModeAccessService;

    private static final String META_CANCELLATION_STATUS = "cancellationRequestStatus";
    private static final String META_CANCELLATION_REQUESTED_BY_ID = "cancellationRequestedByUserId";
    private static final String META_CANCELLATION_REQUESTED_BY_NAME = "cancellationRequestedByName";
    private static final String META_CANCELLATION_REQUESTED_AT = "cancellationRequestedAt";
    private static final String META_CANCELLATION_REASON = "cancellationRequestReason";
    private static final String META_CANCELLATION_RESPONDED_BY_ID = "cancellationRespondedByUserId";
    private static final String META_CANCELLATION_RESPONDED_BY_NAME = "cancellationRespondedByName";
    private static final String META_CANCELLATION_RESPONDED_AT = "cancellationRespondedAt";
    private static final String META_CANCELLATION_RESPONSE_NOTE = "cancellationResponseNote";

    @Override
    @Transactional
    public ContractResponse create(ContractCreateRequest request) {
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
                
        User client = userRepository.findById(request.clientId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                
        User mentor = userRepository.findById(request.mentorId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Proposal proposal = null;
        if (request.proposalId() != null) {
            proposal = proposalRepository.findById(request.proposalId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROPOSAL_NOT_FOUND));
        }

        Contract contract = new Contract();
        contract.setJob(job);
        contract.setProposal(proposal);
        contract.setClient(client);
        contract.setMentor(mentor);
        contract.setStatus(ContractStatus.DRAFT);
        updateContractFields(contract, request);
        
        return toResponse(contractRepository.save(contract));
    }

    @Override
    public ContractResponse getById(UUID contractId) {
        Contract contract = findContract(contractId);
        requireContractReadAccess(contract);
        return toResponse(contract);
    }

    @Override
    public ContractResponse getCurrentMentorContract(UUID contractId) {
        UUID currentMentorId = mentorModeAccessService.getCurrentUserId();
        mentorModeAccessService.requireApprovedMentorContentAccess(currentMentorId);
        Contract contract = findContract(contractId);
        if (!contract.getMentor().getId().equals(currentMentorId) && !mentorModeAccessService.isCurrentUserAdmin()) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "You can only view your own contracts.");
        }
        return toResponse(contract);
    }

    @Override
    @Transactional
    public ContractResponse update(UUID contractId, ContractCreateRequest request) {
        Contract contract = findContract(contractId);
        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        updateContractFields(contract, request);
        return toResponse(contractRepository.save(contract));
    }

    @Override
    public Page<ContractResponse> getByJob(UUID jobId, Pageable pageable) {
        return contractRepository.findByJobId(jobId, pageable).map(this::toResponse);
    }

    @Override
    public Page<ContractResponse> getByClient(UUID clientId, Pageable pageable) {
        mentorModeAccessService.requireSelfOrAdmin(clientId);
        return contractRepository.findByClientId(clientId, pageable).map(this::toResponse);
    }

    @Override
    public Page<ContractResponse> getByMentor(UUID mentorId, Pageable pageable) {
        mentorModeAccessService.requireApprovedMentorContentAccess(mentorId);
        return contractRepository.findByMentorId(mentorId, pageable).map(this::toResponse);
    }

    @Override
    public Page<ContractResponse> getCurrentMentorContracts(Pageable pageable) {
        UUID currentMentorId = mentorModeAccessService.getCurrentUserId();
        mentorModeAccessService.requireApprovedMentorContentAccess(currentMentorId);
        return contractRepository.findByMentorId(currentMentorId, pageable).map(this::toResponse);
    }

    @Override
    public Page<ContractResponse> getByStatus(ContractStatus status, Pageable pageable) {
        return contractRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public ContractResponse signByClient(UUID contractId, String signature, String ipAddress) {
        Contract contract = findContract(contractId);
        contract.setClientSignature(signature);
        contract.setClientSignedAt(LocalDateTime.now());
        contract.setClientSignIp(ipAddress);
        return toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public ContractResponse signByMentor(UUID contractId, String signature, String ipAddress) {
        Contract contract = findContract(contractId);
        contract.setMentorSignature(signature);
        contract.setMentorSignedAt(LocalDateTime.now());
        contract.setMentorSignIp(ipAddress);
        return toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public ContractResponse activate(UUID contractId) {
        Contract contract = findContract(contractId);
        contract.activate();
        return toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public ContractResponse complete(UUID contractId) {
        Contract contract = findContractForUpdate(contractId);
        requireCurrentClient(contract.getClient().getId());
        if (contract.getStatus() == ContractStatus.IN_DISPUTE) {
            throw new AppException(ErrorCode.BAD_REQUEST, "This contract is in dispute and cannot be completed.");
        }
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Only active contracts can be completed.");
        }
        if (!Boolean.TRUE.equals(contract.getFundsInEscrow()) || contract.getAmountInEscrow().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.BAD_REQUEST, "No escrow funds are available to release for this contract.");
        }
        if (contract.getJob().getStatus() != JobStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Only in-progress jobs can be completed.");
        }

        if (Boolean.TRUE.equals(contract.getFundsInEscrow()) && contract.getAmountInEscrow().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal escrowAmount = contract.getAmountInEscrow();
            walletService.releaseContractPayment(
                    contract.getId(),
                    contract.getMentor().getId(),
                    escrowAmount,
                    "Release escrow after client confirmed job completion"
            );
            contract.setAmountPaid(contract.getAmountPaid().add(escrowAmount));
            contract.setAmountInEscrow(BigDecimal.ZERO);
            contract.setFundsInEscrow(false);
        }
        contract.complete();
        Job job = contract.getJob();
        job.setStatus(JobStatus.COMPLETED);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);
        return toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public ContractResponse cancel(UUID contractId, UUID userId, String reason) {
        Contract contract = findContract(contractId);
        requireCurrentUser(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        contract.cancel(user, reason);
        return toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public ContractResponse requestCancellation(UUID contractId, ContractCancellationRequest request) {
        Contract contract = findContractForUpdate(contractId);
        requireCurrentUser(request.requesterId());
        User requester = userRepository.findById(request.requesterId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!contract.getClient().getId().equals(requester.getId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Only the client can request cancellation for this job.");
        }
        if (contract.getStatus() == ContractStatus.IN_DISPUTE) {
            throw new AppException(ErrorCode.BAD_REQUEST, "This contract is currently in dispute.");
        }
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Only active contracts can request cancellation.");
        }
        if (isCancellationPending(contract)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "A cancellation request is already waiting for mentor approval.");
        }

        Map<String, Object> metadata = mutableMetadata(contract);
        metadata.put(META_CANCELLATION_STATUS, "PENDING");
        metadata.put(META_CANCELLATION_REQUESTED_BY_ID, requester.getId().toString());
        metadata.put(META_CANCELLATION_REQUESTED_BY_NAME, requester.getFullName());
        metadata.put(META_CANCELLATION_REQUESTED_AT, LocalDateTime.now().toString());
        metadata.put(META_CANCELLATION_REASON, request.reason().trim());
        metadata.remove(META_CANCELLATION_RESPONDED_BY_ID);
        metadata.remove(META_CANCELLATION_RESPONDED_BY_NAME);
        metadata.remove(META_CANCELLATION_RESPONDED_AT);
        metadata.remove(META_CANCELLATION_RESPONSE_NOTE);
        contract.setMetadata(metadata);

        Contract savedContract = contractRepository.save(contract);
        sendCancellationRequestNotification(savedContract, requester);
        return toResponse(savedContract);
    }

    @Override
    @Transactional
    public ContractResponse approveCancellation(UUID contractId, ContractCancellationDecisionRequest request) {
        Contract contract = findContractForUpdate(contractId);
        requireCurrentUser(request.mentorId());
        User mentor = userRepository.findById(request.mentorId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        requireMentorApprovalAccess(contract, mentor);
        ensureCancellationPending(contract);

        Map<String, Object> metadata = mutableMetadata(contract);
        metadata.put(META_CANCELLATION_STATUS, "APPROVED");
        metadata.put(META_CANCELLATION_RESPONDED_BY_ID, mentor.getId().toString());
        metadata.put(META_CANCELLATION_RESPONDED_BY_NAME, mentor.getFullName());
        metadata.put(META_CANCELLATION_RESPONDED_AT, LocalDateTime.now().toString());
        metadata.put(META_CANCELLATION_RESPONSE_NOTE, request.note().trim());
        contract.setMetadata(metadata);

        if (Boolean.TRUE.equals(contract.getFundsInEscrow()) && contract.getAmountInEscrow().compareTo(BigDecimal.ZERO) > 0) {
            walletService.processRefund(contract.getId(), contract.getClient().getId(), contract.getAmountInEscrow());
            contract.setAmountInEscrow(BigDecimal.ZERO);
            contract.setFundsInEscrow(false);
        }

        String cancellationReason = metadataValue(metadata, META_CANCELLATION_REASON);
        contract.cancel(mentor, buildApprovedCancellationReason(cancellationReason, request.note()));

        Proposal linkedProposal = contract.getProposal();
        if (linkedProposal != null) {
            linkedProposal.markContractCancelled("The linked contract was cancelled after mentor approval.");
            proposalRepository.save(linkedProposal);
        }

        Job job = contract.getJob();
        job.setStatus(JobStatus.OPEN);
        job.setStatusReason("Reopened after the previous mentor contract was cancelled by mutual agreement.");
        job.setClosedAt(null);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        Contract savedContract = contractRepository.save(contract);
        sendCancellationApprovedNotification(savedContract, mentor, request.note().trim());
        return toResponse(savedContract);
    }

    @Override
    @Transactional
    public ContractResponse rejectCancellation(UUID contractId, ContractCancellationDecisionRequest request) {
        Contract contract = findContractForUpdate(contractId);
        requireCurrentUser(request.mentorId());
        User mentor = userRepository.findById(request.mentorId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        requireMentorApprovalAccess(contract, mentor);
        ensureCancellationPending(contract);

        Map<String, Object> metadata = mutableMetadata(contract);
        metadata.put(META_CANCELLATION_STATUS, "REJECTED");
        metadata.put(META_CANCELLATION_RESPONDED_BY_ID, mentor.getId().toString());
        metadata.put(META_CANCELLATION_RESPONDED_BY_NAME, mentor.getFullName());
        metadata.put(META_CANCELLATION_RESPONDED_AT, LocalDateTime.now().toString());
        metadata.put(META_CANCELLATION_RESPONSE_NOTE, request.note().trim());
        contract.setMetadata(metadata);

        Contract savedContract = contractRepository.save(contract);
        sendCancellationRejectedNotification(savedContract, mentor, request.note().trim());
        return toResponse(savedContract);
    }

    private void updateContractFields(Contract contract, ContractCreateRequest request) {
        contract.setTitle(request.title());
        contract.setDescription(request.description());
        contract.setTotalAmount(request.totalAmount());
        contract.setHourlyRate(request.hourlyRate());
        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());
        contract.setDeadlineAt(request.endDate() == null ? null : request.endDate().atStartOfDay());
        contract.setTermsAndConditions(request.termsAndConditions());
        contract.setPaymentTerms(request.paymentTerms());
        contract.setDeliverables(request.deliverables());
        contract.setScopeDescription(request.deliverables());
        if (request.isRenewable() != null) contract.setIsRenewable(request.isRenewable());
        if (request.autoRenewal() != null) contract.setAutoRenewal(request.autoRenewal());
        contract.setRenewalTerms(request.renewalTerms());
        if (request.ndaRequired() != null) contract.setNdaRequired(request.ndaRequired());
    }

    private Contract findContract(UUID contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));
    }

    private Contract findContractForUpdate(UUID contractId) {
        return contractRepository.findByIdForUpdate(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));
    }

    private void requireCurrentUser(UUID userId) {
        if (!mentorModeAccessService.getCurrentUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "You cannot act on behalf of another user.");
        }
    }

    private void requireCurrentClient(UUID clientId) {
        if (!mentorModeAccessService.isCurrentUserAdmin() && !mentorModeAccessService.getCurrentUserId().equals(clientId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Only the contract client can complete this contract.");
        }
    }

    private void requireContractReadAccess(Contract contract) {
        if (mentorModeAccessService.isCurrentUserAdmin()) {
            return;
        }

        UUID currentUserId = mentorModeAccessService.getCurrentUserId();
        if (contract.getClient().getId().equals(currentUserId)) {
            return;
        }

        if (contract.getMentor().getId().equals(currentUserId)) {
            mentorModeAccessService.requireApprovedMentorContentAccess(currentUserId);
            return;
        }

        throw new AppException(ErrorCode.ACCESS_DENIED, "You cannot view this contract.");
    }

    private void requireMentorApprovalAccess(Contract contract, User mentor) {
        if (!contract.getMentor().getId().equals(mentor.getId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Only the assigned mentor can respond to this cancellation request.");
        }
        if (contract.getStatus() == ContractStatus.IN_DISPUTE) {
            throw new AppException(ErrorCode.BAD_REQUEST, "This contract can no longer process cancellation requests.");
        }
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Only active contracts can process cancellation requests.");
        }
    }

    private void ensureCancellationPending(Contract contract) {
        if (!isCancellationPending(contract)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "No pending cancellation request was found for this contract.");
        }
    }

    private boolean isCancellationPending(Contract contract) {
        return "PENDING".equalsIgnoreCase(metadataValue(contract.getMetadata(), META_CANCELLATION_STATUS));
    }

    private Map<String, Object> mutableMetadata(Contract contract) {
        return contract.getMetadata() == null ? new HashMap<>() : new HashMap<>(contract.getMetadata());
    }

    private String metadataValue(Map<String, Object> metadata, String key) {
        if (metadata == null) {
            return null;
        }
        Object value = metadata.get(key);
        return value == null ? null : value.toString();
    }

    private LocalDateTime metadataDateTime(Map<String, Object> metadata, String key) {
        String value = metadataValue(metadata, key);
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
    }

    private UUID metadataUuid(Map<String, Object> metadata, String key) {
        String value = metadataValue(metadata, key);
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }

    private String buildApprovedCancellationReason(String requestReason, String approvalNote) {
        if (approvalNote == null || approvalNote.isBlank()) {
            return "Client cancellation approved by mentor. Reason: " + requestReason;
        }
        return "Client cancellation approved by mentor. Request: " + requestReason + ". Mentor note: " + approvalNote;
    }

    private void sendCancellationRequestNotification(Contract contract, User requester) {
        notificationService.sendNotification(new NotificationCreateRequest(
                contract.getMentor().getId(),
                NotificationType.CONTRACT_CANCELLED,
                "Client requested job cancellation",
                requester.getFullName() + " wants to cancel the job \"" + contract.getJob().getTitle() + "\" and is waiting for your approval.",
                contract.getId(),
                "CONTRACT",
                contract.getProposal() != null
                        ? "/mentor/proposals/" + contract.getProposal().getId()
                        : "/mentor/proposals",
                null,
                1,
                null,
                "CONTRACT",
                contract.getId().toString(),
                requester.getId()
        ));
    }

    private void sendCancellationApprovedNotification(Contract contract, User mentor, String note) {
        notificationService.sendNotification(new NotificationCreateRequest(
                contract.getClient().getId(),
                NotificationType.CONTRACT_CANCELLED,
                "Mentor approved your cancellation request",
                mentor.getFullName() + " agreed to cancel \"" + contract.getJob().getTitle() + "\". Escrow has been refunded to your wallet and the job is open again for new mentors." +
                        (note == null || note.isBlank() ? "" : " Note: " + note),
                contract.getId(),
                "CONTRACT",
                "/my-jobs/" + contract.getJob().getId(),
                null,
                1,
                null,
                "CONTRACT",
                contract.getId().toString(),
                mentor.getId()
        ));
    }

    private void sendCancellationRejectedNotification(Contract contract, User mentor, String note) {
        notificationService.sendNotification(new NotificationCreateRequest(
                contract.getClient().getId(),
                NotificationType.CONTRACT_CANCELLED,
                "Mentor declined your cancellation request",
                mentor.getFullName() + " wants to continue the job \"" + contract.getJob().getTitle() + "\"." +
                        (note == null || note.isBlank() ? "" : " Note: " + note),
                contract.getId(),
                "CONTRACT",
                "/my-jobs/" + contract.getJob().getId(),
                null,
                1,
                null,
                "CONTRACT",
                contract.getId().toString(),
                mentor.getId()
        ));
    }

    private ContractResponse toResponse(Contract contract) {
        Map<String, Object> metadata = contract.getMetadata();
        return new ContractResponse(
                contract.getId(),
                contract.getJob().getId(),
                contract.getJob().getTitle(),
                contract.getProposal() != null ? contract.getProposal().getId() : null,
                contract.getClient().getId(),
                contract.getClient().getFullName(),
                contract.getMentor().getId(),
                contract.getMentor().getFullName(),
                contract.getStatus(),
                contract.getTitle(),
                contract.getDescription(),
                contract.getTotalAmount(),
                contract.getHourlyRate(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getDeadlineAt(),
                contract.getScopeDescription(),
                contract.getActualStartDate(),
                contract.getActualCompletionDate(),
                contract.getTermsAndConditions(),
                contract.getPaymentTerms(),
                contract.getDeliverables(),
                contract.getClientSignedAt(),
                contract.getMentorSignedAt(),
                contract.getActivatedAt(),
                contract.getCompletedAt(),
                contract.getCancelledAt(),
                metadataValue(metadata, META_CANCELLATION_STATUS),
                metadataUuid(metadata, META_CANCELLATION_REQUESTED_BY_ID),
                metadataValue(metadata, META_CANCELLATION_REQUESTED_BY_NAME),
                metadataDateTime(metadata, META_CANCELLATION_REQUESTED_AT),
                metadataValue(metadata, META_CANCELLATION_REASON),
                metadataUuid(metadata, META_CANCELLATION_RESPONDED_BY_ID),
                metadataValue(metadata, META_CANCELLATION_RESPONDED_BY_NAME),
                metadataDateTime(metadata, META_CANCELLATION_RESPONDED_AT),
                metadataValue(metadata, META_CANCELLATION_RESPONSE_NOTE),
                contract.getMilestoneCount(),
                contract.getCompletedMilestoneCount(),
                contract.getAmountPaid(),
                contract.getAmountInEscrow(),
                contract.getFundsInEscrow(),
                contract.getProgressPercentage(),
                contract.getIsRenewable(),
                contract.getAutoRenewal(),
                contract.getRenewalTerms(),
                contract.getNdaRequired(),
                contract.getNdaSigned(),
                contract.getCreatedAt(),
                contract.getUpdatedAt()
        );
    }
}
