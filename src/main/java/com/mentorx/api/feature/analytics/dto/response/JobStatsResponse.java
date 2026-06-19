package com.mentorx.api.feature.analytics.dto.response;

import com.mentorx.api.feature.analytics.enums.JobStatsRole;

import java.math.BigDecimal;
import java.util.UUID;

public record JobStatsResponse(
        UUID userId,
        JobStatsRole role,

        // Mentor view
        Integer proposalsSent,
        Integer proposalsAccepted,
        Integer proposalsRejected,
        Integer proposalsPending,
        Double proposalAcceptanceRate,
        Integer contractsActive,
        Integer contractsCompleted,
        Integer contractsCancelled,
        Double contractCompletionRate,
        BigDecimal averageContractValueMxc,

        // Client view
        Integer jobsPosted,
        Integer jobsOpen,
        Integer jobsInProgress,
        Integer jobsCompleted,
        Integer jobsCancelled,
        Integer totalProposalsReceived,
        Double averageProposalsPerJob
) {
    public static JobStatsResponse emptyMentor(UUID userId) {
        return new JobStatsResponse(
                userId, JobStatsRole.MENTOR,
                0, 0, 0, 0, 0.0,
                0, 0, 0, 0.0, BigDecimal.ZERO,
                null, null, null, null, null, null, null
        );
    }

    public static JobStatsResponse emptyClient(UUID userId) {
        return new JobStatsResponse(
                userId, JobStatsRole.CLIENT,
                null, null, null, null, null,
                null, null, null, null, null,
                0, 0, 0, 0, 0, 0, 0.0
        );
    }
}
