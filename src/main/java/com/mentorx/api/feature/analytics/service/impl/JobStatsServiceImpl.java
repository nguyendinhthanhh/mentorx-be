package com.mentorx.api.feature.analytics.service.impl;

import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.feature.analytics.dto.response.JobStatsResponse;
import com.mentorx.api.feature.analytics.enums.JobStatsRole;
import com.mentorx.api.feature.analytics.service.JobStatsService;
import com.mentorx.api.feature.job.enums.ContractStatus;
import com.mentorx.api.feature.job.enums.ProposalStatus;
import com.mentorx.api.feature.job.repository.ContractRepository;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.job.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobStatsServiceImpl implements JobStatsService {

    private final ProposalRepository proposalRepository;
    private final ContractRepository contractRepository;
    private final JobRepository jobRepository;

    @Override
    public JobStatsResponse getStats(UUID userId, JobStatsRole role) {
        if (role == JobStatsRole.CLIENT) {
            return clientStats(userId);
        }
        return mentorStats(userId);
    }

    private JobStatsResponse mentorStats(UUID mentorId) {
        long sent = proposalRepository.countByMentorId(mentorId);
        long accepted = proposalRepository.countByMentorIdAndStatus(mentorId, ProposalStatus.ACCEPTED);
        long rejected = proposalRepository.countByMentorIdAndStatus(mentorId, ProposalStatus.REJECTED)
                + proposalRepository.countByMentorIdAndStatus(mentorId, ProposalStatus.AUTO_CLOSED)
                + proposalRepository.countByMentorIdAndStatus(mentorId, ProposalStatus.WITHDRAWN)
                + proposalRepository.countByMentorIdAndStatus(mentorId, ProposalStatus.CONTRACT_CANCELLED);
        long pending = proposalRepository.countPendingByMentorId(mentorId);

        double acceptanceRate = sent == 0 ? 0.0 :
                BigDecimal.valueOf(accepted)
                        .divide(BigDecimal.valueOf(sent), 4, RoundingMode.HALF_UP)
                        .doubleValue();

        long active = contractRepository.countByMentorIdAndStatus(mentorId, ContractStatus.ACTIVE)
                + contractRepository.countByMentorIdAndStatus(mentorId, ContractStatus.PENDING_SIGNATURE)
                + contractRepository.countByMentorIdAndStatus(mentorId, ContractStatus.UNDER_REVIEW)
                + contractRepository.countByMentorIdAndStatus(mentorId, ContractStatus.PENDING_PAYMENT);
        long completed = contractRepository.countByMentorIdAndStatus(mentorId, ContractStatus.COMPLETED);
        long cancelled = contractRepository.countByMentorIdAndStatus(mentorId, ContractStatus.CANCELLED)
                + contractRepository.countByMentorIdAndStatus(mentorId, ContractStatus.TERMINATED)
                + contractRepository.countByMentorIdAndStatus(mentorId, ContractStatus.EXPIRED);

        double completionRate = (completed + cancelled) == 0 ? 0.0 :
                BigDecimal.valueOf(completed)
                        .divide(BigDecimal.valueOf(completed + cancelled), 4, RoundingMode.HALF_UP)
                        .doubleValue();

        BigDecimal avgAmount = contractRepository.averageCompletedAmountByMentorId(mentorId);
        if (avgAmount == null) {
            avgAmount = BigDecimal.ZERO;
        }

        return new JobStatsResponse(
                mentorId, JobStatsRole.MENTOR,
                (int) sent, (int) accepted, (int) rejected, (int) pending, acceptanceRate,
                (int) active, (int) completed, (int) cancelled, completionRate, avgAmount,
                null, null, null, null, null, null, null
        );
    }

    private JobStatsResponse clientStats(UUID clientId) {
        long posted = jobRepository.countByClientIdAndDeletedAtIsNull(clientId);
        long open = jobRepository.countByClientIdAndStatus(clientId, JobStatus.OPEN);
        long inProgress = jobRepository.countByClientIdAndStatus(clientId, JobStatus.IN_PROGRESS);
        long completed = jobRepository.countByClientIdAndStatus(clientId, JobStatus.COMPLETED);
        long cancelled = jobRepository.countByClientIdAndStatus(clientId, JobStatus.CANCELLED);

        long proposalsReceived = proposalRepository.countProposalsForJobsByClientId(clientId);
        double avgPerJob = posted == 0 ? 0.0 :
                BigDecimal.valueOf(proposalsReceived)
                        .divide(BigDecimal.valueOf(posted), 4, RoundingMode.HALF_UP)
                        .doubleValue();

        return new JobStatsResponse(
                clientId, JobStatsRole.CLIENT,
                null, null, null, null, null,
                null, null, null, null, null,
                (int) posted, (int) open, (int) inProgress, (int) completed, (int) cancelled,
                (int) proposalsReceived, avgPerJob
        );
    }
}
