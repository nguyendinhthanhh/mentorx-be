package com.mentorx.api.feature.job.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.job.dto.request.MilestoneCreateRequest;
import com.mentorx.api.feature.job.dto.response.MilestoneResponse;
import com.mentorx.api.feature.job.entity.Contract;
import com.mentorx.api.feature.job.entity.Milestone;
import com.mentorx.api.feature.job.enums.MilestoneStatus;
import com.mentorx.api.feature.job.repository.ContractRepository;
import com.mentorx.api.feature.job.repository.MilestoneRepository;
import com.mentorx.api.feature.job.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ContractRepository contractRepository;

    @Override
    @Transactional
    public MilestoneResponse create(MilestoneCreateRequest request) {
        Contract contract = contractRepository.findById(request.contractId())
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        Milestone milestone = new Milestone();
        milestone.setContract(contract);
        milestone.setStatus(MilestoneStatus.PENDING);
        updateMilestoneFields(milestone, request);
        
        Milestone saved = milestoneRepository.save(milestone);
        contract.setMilestoneCount(contract.getMilestoneCount() + 1);
        contractRepository.save(contract);
        
        return toResponse(saved);
    }

    @Override
    public MilestoneResponse getById(UUID milestoneId) {
        return toResponse(findMilestone(milestoneId));
    }

    @Override
    @Transactional
    public MilestoneResponse update(UUID milestoneId, MilestoneCreateRequest request) {
        Milestone milestone = findMilestone(milestoneId);
        if (milestone.getStatus() != MilestoneStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_MILESTONE_STATUS);
        }
        updateMilestoneFields(milestone, request);
        return toResponse(milestoneRepository.save(milestone));
    }

    @Override
    @Transactional
    public void delete(UUID milestoneId) {
        Milestone milestone = findMilestone(milestoneId);
        if (milestone.getStatus() != MilestoneStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_MILESTONE_STATUS);
        }
        Contract contract = milestone.getContract();
        contract.setMilestoneCount(contract.getMilestoneCount() - 1);
        contractRepository.save(contract);
        milestoneRepository.deleteById(milestoneId);
    }

    @Override
    public Page<MilestoneResponse> getByContract(UUID contractId, Pageable pageable) {
        return milestoneRepository.findByContractId(contractId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public MilestoneResponse start(UUID milestoneId) {
        Milestone milestone = findMilestone(milestoneId);
        milestone.start();
        return toResponse(milestoneRepository.save(milestone));
    }

    @Override
    @Transactional
    public MilestoneResponse submit(UUID milestoneId, String notes) {
        Milestone milestone = findMilestone(milestoneId);
        milestone.submit(notes);
        return toResponse(milestoneRepository.save(milestone));
    }

    @Override
    @Transactional
    public MilestoneResponse approve(UUID milestoneId, String notes) {
        Milestone milestone = findMilestone(milestoneId);
        milestone.approve(notes);
        return toResponse(milestoneRepository.save(milestone));
    }

    @Override
    @Transactional
    public MilestoneResponse requestRevision(UUID milestoneId, String notes) {
        Milestone milestone = findMilestone(milestoneId);
        milestone.requestRevision(notes);
        return toResponse(milestoneRepository.save(milestone));
    }

    @Override
    @Transactional
    public MilestoneResponse complete(UUID milestoneId, Long transactionId) {
        Milestone milestone = findMilestone(milestoneId);
        milestone.complete(transactionId);
        Milestone saved = milestoneRepository.save(milestone);
        
        Contract contract = saved.getContract();
        contract.setCompletedMilestoneCount(contract.getCompletedMilestoneCount() + 1);
        contract.updateProgress();
        contractRepository.save(contract);
        
        return toResponse(saved);
    }

    private void updateMilestoneFields(Milestone milestone, MilestoneCreateRequest request) {
        milestone.setTitle(request.title());
        milestone.setDescription(request.description());
        milestone.setMilestoneOrder(request.milestoneOrder());
        milestone.setAmount(request.amount());
        milestone.setDueDate(request.dueDate());
        if (request.maxRevisions() != null) milestone.setMaxRevisions(request.maxRevisions());
        if (request.isOptional() != null) milestone.setIsOptional(request.isOptional());
        if (request.dependsOnPrevious() != null) milestone.setDependsOnPrevious(request.dependsOnPrevious());
    }

    private Milestone findMilestone(UUID milestoneId) {
        return milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new AppException(ErrorCode.MILESTONE_NOT_FOUND));
    }

    private MilestoneResponse toResponse(Milestone milestone) {
        return new MilestoneResponse(
                milestone.getId(),
                milestone.getContract().getId(),
                milestone.getContract().getTitle(),
                milestone.getTitle(),
                milestone.getDescription(),
                milestone.getMilestoneOrder(),
                milestone.getStatus(),
                milestone.getAmount(),
                milestone.getDueDate(),
                milestone.getStartedAt(),
                milestone.getSubmittedAt(),
                milestone.getApprovedAt(),
                milestone.getCompletedAt(),
                milestone.getSubmissionNotes(),
                milestone.getReviewNotes(),
                milestone.getRevisionCount(),
                milestone.getMaxRevisions(),
                milestone.getPaymentReleased(),
                milestone.getPaymentReleasedAt(),
                milestone.getPaymentTransactionId(),
                milestone.getIsOptional(),
                milestone.getDependsOnPrevious(),
                milestone.getCreatedAt(),
                milestone.getUpdatedAt()
        );
    }
}
