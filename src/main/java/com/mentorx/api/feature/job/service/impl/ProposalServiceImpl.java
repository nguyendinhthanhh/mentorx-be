package com.mentorx.api.feature.job.service.impl;

import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.feature.job.dto.request.ProposalCreateRequest;
import com.mentorx.api.feature.job.dto.response.ProposalResponse;
import com.mentorx.api.feature.job.entity.Contract;
import com.mentorx.api.feature.job.entity.Job;
import com.mentorx.api.feature.job.entity.Proposal;
import com.mentorx.api.feature.job.entity.ProposalNegotiation;
import com.mentorx.api.feature.job.enums.ContractStatus;
import com.mentorx.api.feature.job.enums.ProposalStatus;
import com.mentorx.api.feature.job.repository.ContractRepository;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.job.repository.ProposalNegotiationRepository;
import com.mentorx.api.feature.job.repository.ProposalRepository;
import com.mentorx.api.feature.job.service.ProposalService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.notification.service.NotificationService;
import com.mentorx.api.feature.notification.dto.request.NotificationCreateRequest;
import com.mentorx.api.feature.notification.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import com.mentorx.api.feature.wallet.service.WalletService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProposalServiceImpl implements ProposalService {

    private final ProposalRepository proposalRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final ProposalNegotiationRepository proposalNegotiationRepository;
    private final MentorModeAccessService mentorModeAccessService;
    private final NotificationService notificationService;
    private final WalletService walletService;

    @Override
    @Transactional
    public ProposalResponse create(ProposalCreateRequest request) {
        mentorModeAccessService.requireApprovedMentorContentAccess(request.mentorId());
        if (proposalRepository.findByJobIdAndMentorId(request.jobId(), request.mentorId()).isPresent()) {
            throw new AppException(ErrorCode.PROPOSAL_ALREADY_EXISTS);
        }
        
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
                
        if (job.getStatus() != com.mentorx.api.common.enums.JobStatus.OPEN) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Cannot propose to a job that is not open.");
        }

        if (job.getClient().getId().equals(request.mentorId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Client cannot submit a proposal to their own job.");
        }
                
        User mentor = userRepository.findById(request.mentorId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Proposal proposal = new Proposal();
        proposal.setJob(job);
        proposal.setMentor(mentor);
        proposal.setStatus(ProposalStatus.DRAFT);
        updateProposalFields(proposal, request);
        
        return toResponse(proposalRepository.save(proposal));
    }

    @Override
    public ProposalResponse getById(UUID proposalId) {
        return toResponse(findProposal(proposalId));
    }

    @Override
    @Transactional
    public ProposalResponse update(UUID proposalId, ProposalCreateRequest request) {
        Proposal proposal = findProposal(proposalId);
        mentorModeAccessService.requireApprovedMentorContentAccess(proposal.getMentor().getId());
        if (proposal.getStatus() != ProposalStatus.DRAFT && proposal.getStatus() != ProposalStatus.WITHDRAWN) {
            throw new AppException(ErrorCode.BAD_REQUEST); // Only draft or withdrawn can be updated directly
        }
        updateProposalFields(proposal, request);
        return toResponse(proposalRepository.save(proposal));
    }

    @Override
    @Transactional
    public void delete(UUID proposalId) {
        Proposal proposal = findProposal(proposalId);
        mentorModeAccessService.requireApprovedMentorContentAccess(proposal.getMentor().getId());
        proposalRepository.delete(proposal);
    }

    @Override
    public ProposalResponse getByJobAndMentor(UUID jobId, UUID mentorId) {
        return proposalRepository.findByJobIdAndMentorId(jobId, mentorId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public void withdraw(UUID proposalId) {
        Proposal proposal = findProposal(proposalId);
        mentorModeAccessService.requireApprovedMentorContentAccess(proposal.getMentor().getId());
        if (proposal.getStatus() == ProposalStatus.ACCEPTED) {
            throw new AppException(ErrorCode.BAD_REQUEST); // Cannot withdraw accepted proposal
        }
        proposal.withdraw();
        proposalRepository.save(proposal);
    }

    @Override
    public Page<ProposalResponse> getByJob(UUID jobId, Pageable pageable) {
        return proposalRepository.findByJobId(jobId, pageable).map(this::toResponse);
    }

    @Override
    public Page<ProposalResponse> getByMentor(UUID mentorId, Pageable pageable) {
        mentorModeAccessService.requireSelfOrAdmin(mentorId);
        return proposalRepository.findByMentorId(mentorId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public ProposalResponse submit(UUID proposalId) {
        Proposal proposal = findProposal(proposalId);
        mentorModeAccessService.requireApprovedMentorContentAccess(proposal.getMentor().getId());
        
        if (proposal.getJob().getStatus() != com.mentorx.api.common.enums.JobStatus.OPEN) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Cannot submit proposal because the job is no longer open.");
        }
        
        proposal.submit();
        
        // Update job activity
        Job job = proposal.getJob();
        job.setProposalCount(job.getProposalCount() + 1);
        job.setUpdatedAt(java.time.LocalDateTime.now());
        jobRepository.save(job);
        
        // Notify the client
        NotificationCreateRequest notificationReq = new NotificationCreateRequest(
                job.getClient().getId(),
                NotificationType.JOB_APPLICATION_RECEIVED,
                "Ứng viên mới cho: " + job.getTitle(),
                proposal.getMentor().getFullName() + " đã gửi đề xuất cho công việc của bạn.",
                proposal.getId(),
                "PROPOSAL",
                "/jobs/" + job.getId(),
                null,
                1,
                null,
                "JOB",
                job.getId().toString(),
                proposal.getMentor().getId()
        );
        notificationService.sendNotification(notificationReq);
        
        return toResponse(proposalRepository.save(proposal));
    }

    @Override
    @Transactional
    public ProposalResponse accept(UUID proposalId) {
        Proposal proposal = findProposal(proposalId);
        if (proposal.getStatus() == ProposalStatus.ACCEPTED) {
            return toResponse(proposal);
        }

        if (contractRepository.findByProposalId(proposalId).isPresent()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "A contract already exists for this proposal.");
        }

        Job job = proposal.getJob();
        if (job.getStatus() != JobStatus.OPEN && job.getStatus() != JobStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.BAD_REQUEST, "This job is no longer accepting proposal decisions.");
        }

        Optional<ProposalNegotiation> latestNegotiation = proposalNegotiationRepository.findLatestByProposalId(proposalId);
        BigDecimal finalAmount = latestNegotiation
                .map(ProposalNegotiation::getProposedAmount)
                .filter(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                .orElse(proposal.getProposedAmount());
        Integer finalDurationDays = latestNegotiation
                .map(ProposalNegotiation::getEstimatedDurationDays)
                .orElse(proposal.getEstimatedDurationDays());

        applyAcceptedTerms(proposal, latestNegotiation.orElse(null), finalAmount, finalDurationDays);
        proposal.accept();

        Contract contract = buildAcceptedContract(proposal, finalAmount, finalDurationDays);
        Contract savedContract = contractRepository.save(contract);

        walletService.processJobPayment(job.getClient().getId(), savedContract.getId(), finalAmount);

        savedContract.setAmountInEscrow(finalAmount);
        savedContract.setFundsInEscrow(true);
        savedContract.setStatus(ContractStatus.ACTIVE);
        savedContract.setActivatedAt(LocalDateTime.now());
        contractRepository.save(savedContract);

        latestNegotiation.ifPresent(negotiation -> {
            negotiation.accept();
            proposalNegotiationRepository.save(negotiation);
        });

        job.setStatus(JobStatus.IN_PROGRESS);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);
        
        return toResponse(proposalRepository.save(proposal));
    }

    @Override
    @Transactional
    public ProposalResponse reject(UUID proposalId, String reason) {
        Proposal proposal = findProposal(proposalId);
        proposal.reject(reason);
        return toResponse(proposalRepository.save(proposal));
    }

    @Override
    @Transactional
    public void markAsViewed(UUID proposalId) {
        Proposal proposal = findProposal(proposalId);
        proposal.markAsViewed();
        proposalRepository.save(proposal);
    }

    private void updateProposalFields(Proposal proposal, ProposalCreateRequest request) {
        proposal.setCoverLetter(request.coverLetter());
        proposal.setProposedAmount(request.proposedAmount());
        proposal.setProposedHourlyRate(request.proposedHourlyRate());
        proposal.setEstimatedDurationDays(request.estimatedDurationDays());
        proposal.setProposedStartDate(request.proposedStartDate());
        proposal.setProposedDeliveryDate(request.proposedDeliveryDate());
        if (request.proposedMilestones() != null) proposal.setProposedMilestones(request.proposedMilestones());
        proposal.setRelevantExperience(request.relevantExperience());
        if (request.portfolioLinks() != null) proposal.setPortfolioLinks(request.portfolioLinks());
        if (request.attachments() != null) proposal.setAttachments(request.attachments());
        proposal.setQuestions(request.questions());
        proposal.setTerms(request.terms());
    }

    private Proposal findProposal(UUID proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.PROPOSAL_NOT_FOUND));
    }

    private void applyAcceptedTerms(
            Proposal proposal,
            ProposalNegotiation latestNegotiation,
            BigDecimal finalAmount,
            Integer finalDurationDays
    ) {
        proposal.setProposedAmount(finalAmount);
        proposal.setEstimatedDurationDays(finalDurationDays);

        if (latestNegotiation == null) {
            return;
        }

        if (latestNegotiation.getProposedHourlyRate() != null) {
            proposal.setProposedHourlyRate(latestNegotiation.getProposedHourlyRate());
        }
        if (latestNegotiation.getProposedStartDate() != null) {
            proposal.setProposedStartDate(latestNegotiation.getProposedStartDate());
        }
        if (latestNegotiation.getProposedDeliveryDate() != null) {
            proposal.setProposedDeliveryDate(latestNegotiation.getProposedDeliveryDate());
        }
    }

    private Contract buildAcceptedContract(Proposal proposal, BigDecimal finalAmount, Integer finalDurationDays) {
        Contract contract = new Contract();
        Job job = proposal.getJob();
        LocalDate today = LocalDate.now();

        contract.setJob(job);
        contract.setProposal(proposal);
        contract.setClient(job.getClient());
        contract.setMentor(proposal.getMentor());
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setTitle(job.getTitle());
        contract.setDescription(job.getDescription());
        contract.setTotalAmount(finalAmount);
        contract.setHourlyRate(proposal.getProposedHourlyRate());
        contract.setStartDate(proposal.getProposedStartDate() != null ? proposal.getProposedStartDate() : today);
        contract.setEndDate(resolveContractEndDate(proposal, finalDurationDays, today));
        contract.setActualStartDate(today);
        contract.setActivatedAt(LocalDateTime.now());
        contract.setPaymentTerms("Funds are held in escrow until the client confirms the job is completed.");
        contract.setDeliverables(job.getSuccessCriteria());
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

    private ProposalResponse toResponse(Proposal proposal) {
        return new ProposalResponse(
                proposal.getId(),
                proposal.getJob().getId(),
                proposal.getJob().getTitle(),
                proposal.getMentor().getId(),
                proposal.getMentor().getFullName(),
                proposal.getStatus(),
                proposal.getCoverLetter(),
                proposal.getProposedAmount(),
                proposal.getProposedHourlyRate(),
                proposal.getEstimatedDurationDays(),
                proposal.getProposedStartDate(),
                proposal.getProposedDeliveryDate(),
                proposal.getProposedMilestones(),
                proposal.getRelevantExperience(),
                proposal.getPortfolioLinks() == null ? null : new ArrayList<>(proposal.getPortfolioLinks()),
                proposal.getAttachments() == null ? null : new ArrayList<>(proposal.getAttachments()),
                proposal.getQuestions(),
                proposal.getTerms(),
                proposal.getSubmittedAt(),
                proposal.getIsFeatured(),
                proposal.getScore(),
                proposal.getIsCounterProposal(),
                proposal.getViewCount(),
                proposal.getCreatedAt(),
                proposal.getUpdatedAt()
        );
    }
}
