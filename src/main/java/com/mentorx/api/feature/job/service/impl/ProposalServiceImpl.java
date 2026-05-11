package com.mentorx.api.feature.job.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.job.dto.request.ProposalCreateRequest;
import com.mentorx.api.feature.job.dto.response.ProposalResponse;
import com.mentorx.api.feature.job.entity.Job;
import com.mentorx.api.feature.job.entity.Proposal;
import com.mentorx.api.feature.job.enums.ProposalStatus;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.job.repository.ProposalRepository;
import com.mentorx.api.feature.job.service.ProposalService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProposalServiceImpl implements ProposalService {

    private final ProposalRepository proposalRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProposalResponse create(ProposalCreateRequest request) {
        if (proposalRepository.findByJobIdAndMentorId(request.jobId(), request.mentorId()).isPresent()) {
            throw new AppException(ErrorCode.PROPOSAL_ALREADY_EXISTS);
        }
        
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
                
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
        if (proposal.getStatus() != ProposalStatus.DRAFT && proposal.getStatus() != ProposalStatus.WITHDRAWN) {
            throw new AppException(ErrorCode.BAD_REQUEST); // Only draft or withdrawn can be updated directly
        }
        updateProposalFields(proposal, request);
        return toResponse(proposalRepository.save(proposal));
    }

    @Override
    @Transactional
    public void delete(UUID proposalId) {
        proposalRepository.deleteById(proposalId);
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
        return proposalRepository.findByMentorId(mentorId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public ProposalResponse submit(UUID proposalId) {
        Proposal proposal = findProposal(proposalId);
        proposal.submit();
        return toResponse(proposalRepository.save(proposal));
    }

    @Override
    @Transactional
    public ProposalResponse accept(UUID proposalId) {
        Proposal proposal = findProposal(proposalId);
        proposal.accept();
        
        // Update job status to CLOSED (job is filled)
        Job job = proposal.getJob();
        job.setStatus(com.mentorx.api.common.enums.JobStatus.CLOSED);
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
                proposal.getCreatedAt(),
                proposal.getUpdatedAt()
        );
    }
}
