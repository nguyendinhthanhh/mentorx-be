package com.mentorx.api.feature.job.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
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
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final UserRepository userRepository;

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
        return toResponse(findContract(contractId));
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
        return contractRepository.findByClientId(clientId, pageable).map(this::toResponse);
    }

    @Override
    public Page<ContractResponse> getByMentor(UUID mentorId, Pageable pageable) {
        return contractRepository.findByMentorId(mentorId, pageable).map(this::toResponse);
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
        Contract contract = findContract(contractId);
        contract.complete();
        return toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public ContractResponse cancel(UUID contractId, UUID userId, String reason) {
        Contract contract = findContract(contractId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        contract.cancel(user, reason);
        return toResponse(contractRepository.save(contract));
    }

    private void updateContractFields(Contract contract, ContractCreateRequest request) {
        contract.setTitle(request.title());
        contract.setDescription(request.description());
        contract.setTotalAmount(request.totalAmount());
        contract.setHourlyRate(request.hourlyRate());
        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());
        contract.setTermsAndConditions(request.termsAndConditions());
        contract.setPaymentTerms(request.paymentTerms());
        contract.setDeliverables(request.deliverables());
        if (request.isRenewable() != null) contract.setIsRenewable(request.isRenewable());
        if (request.autoRenewal() != null) contract.setAutoRenewal(request.autoRenewal());
        contract.setRenewalTerms(request.renewalTerms());
        if (request.ndaRequired() != null) contract.setNdaRequired(request.ndaRequired());
    }

    private Contract findContract(UUID contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));
    }

    private ContractResponse toResponse(Contract contract) {
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
