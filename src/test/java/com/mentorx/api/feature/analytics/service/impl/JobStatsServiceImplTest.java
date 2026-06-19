package com.mentorx.api.feature.analytics.service.impl;

import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.feature.analytics.dto.response.JobStatsResponse;
import com.mentorx.api.feature.analytics.enums.JobStatsRole;
import com.mentorx.api.feature.job.enums.ContractStatus;
import com.mentorx.api.feature.job.enums.ProposalStatus;
import com.mentorx.api.feature.job.repository.ContractRepository;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.job.repository.ProposalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobStatsServiceImplTest {

    private ProposalRepository proposalRepository;
    private ContractRepository contractRepository;
    private JobRepository jobRepository;
    private JobStatsServiceImpl service;

    @BeforeEach
    void setUp() {
        proposalRepository = mock(ProposalRepository.class);
        contractRepository = mock(ContractRepository.class);
        jobRepository = mock(JobRepository.class);
        service = new JobStatsServiceImpl(proposalRepository, contractRepository, jobRepository);
    }

    @Test
    void mentorStats_zeroDenominators_yieldZeroRates() {
        UUID userId = UUID.randomUUID();
        when(proposalRepository.countByMentorId(userId)).thenReturn(0L);
        when(proposalRepository.countByMentorIdAndStatus(eq(userId), any())).thenReturn(0L);
        when(proposalRepository.countPendingByMentorId(userId)).thenReturn(0L);
        when(contractRepository.countByMentorIdAndStatus(eq(userId), any())).thenReturn(0L);
        when(contractRepository.averageCompletedAmountByMentorId(userId)).thenReturn(null);

        JobStatsResponse stats = service.getStats(userId, JobStatsRole.MENTOR);

        assertThat(stats.role()).isEqualTo(JobStatsRole.MENTOR);
        assertThat(stats.proposalsSent()).isZero();
        assertThat(stats.proposalsAccepted()).isZero();
        assertThat(stats.proposalAcceptanceRate()).isZero();
        assertThat(stats.contractCompletionRate()).isZero();
        assertThat(stats.averageContractValueMxc()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void mentorStats_knownNumbers_computeRatesCorrectly() {
        UUID userId = UUID.randomUUID();
        when(proposalRepository.countByMentorId(userId)).thenReturn(40L);
        when(proposalRepository.countByMentorIdAndStatus(userId, ProposalStatus.ACCEPTED)).thenReturn(10L);
        when(proposalRepository.countByMentorIdAndStatus(userId, ProposalStatus.REJECTED)).thenReturn(15L);
        when(proposalRepository.countByMentorIdAndStatus(userId, ProposalStatus.AUTO_CLOSED)).thenReturn(5L);
        when(proposalRepository.countByMentorIdAndStatus(userId, ProposalStatus.WITHDRAWN)).thenReturn(2L);
        when(proposalRepository.countByMentorIdAndStatus(userId, ProposalStatus.CONTRACT_CANCELLED)).thenReturn(0L);
        when(proposalRepository.countPendingByMentorId(userId)).thenReturn(8L);

        when(contractRepository.countByMentorIdAndStatus(userId, ContractStatus.ACTIVE)).thenReturn(2L);
        when(contractRepository.countByMentorIdAndStatus(userId, ContractStatus.PENDING_SIGNATURE)).thenReturn(1L);
        when(contractRepository.countByMentorIdAndStatus(userId, ContractStatus.UNDER_REVIEW)).thenReturn(0L);
        when(contractRepository.countByMentorIdAndStatus(userId, ContractStatus.PENDING_PAYMENT)).thenReturn(0L);
        when(contractRepository.countByMentorIdAndStatus(userId, ContractStatus.COMPLETED)).thenReturn(7L);
        when(contractRepository.countByMentorIdAndStatus(userId, ContractStatus.CANCELLED)).thenReturn(1L);
        when(contractRepository.countByMentorIdAndStatus(userId, ContractStatus.TERMINATED)).thenReturn(0L);
        when(contractRepository.countByMentorIdAndStatus(userId, ContractStatus.EXPIRED)).thenReturn(0L);
        when(contractRepository.averageCompletedAmountByMentorId(userId)).thenReturn(new BigDecimal("2500.0000"));

        JobStatsResponse stats = service.getStats(userId, JobStatsRole.MENTOR);

        assertThat(stats.proposalsSent()).isEqualTo(40);
        assertThat(stats.proposalsAccepted()).isEqualTo(10);
        assertThat(stats.proposalsRejected()).isEqualTo(22);
        assertThat(stats.proposalsPending()).isEqualTo(8);
        // 10 / 40 = 0.25
        assertThat(stats.proposalAcceptanceRate()).isEqualTo(0.25);
        // 2 + 1 = 3 active
        assertThat(stats.contractsActive()).isEqualTo(3);
        // 7 / (7 + 1) = 0.875
        assertThat(stats.contractCompletionRate()).isEqualTo(0.875);
        assertThat(stats.averageContractValueMxc()).isEqualByComparingTo(new BigDecimal("2500.0000"));
    }

    @Test
    void clientStats_zeroProposals_yieldZeroAverage() {
        UUID userId = UUID.randomUUID();
        when(jobRepository.countByClientIdAndDeletedAtIsNull(userId)).thenReturn(5L);
        when(jobRepository.countByClientIdAndStatus(userId, JobStatus.OPEN)).thenReturn(2L);
        when(jobRepository.countByClientIdAndStatus(userId, JobStatus.IN_PROGRESS)).thenReturn(1L);
        when(jobRepository.countByClientIdAndStatus(userId, JobStatus.COMPLETED)).thenReturn(1L);
        when(jobRepository.countByClientIdAndStatus(userId, JobStatus.CANCELLED)).thenReturn(0L);
        when(proposalRepository.countProposalsForJobsByClientId(userId)).thenReturn(0L);

        JobStatsResponse stats = service.getStats(userId, JobStatsRole.CLIENT);

        assertThat(stats.role()).isEqualTo(JobStatsRole.CLIENT);
        assertThat(stats.jobsPosted()).isEqualTo(5);
        assertThat(stats.totalProposalsReceived()).isZero();
        assertThat(stats.averageProposalsPerJob()).isZero();
    }
}
