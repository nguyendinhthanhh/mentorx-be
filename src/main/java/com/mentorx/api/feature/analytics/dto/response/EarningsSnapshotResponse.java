package com.mentorx.api.feature.analytics.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EarningsSnapshotResponse(
        UUID id,
        UUID userId,
        LocalDate snapshotDate,
        BigDecimal earnedMxc,
        BigDecimal withdrawnMxc,
        BigDecimal platformFeeMxc,
        Integer jobsCompleted,
        Integer coursesSold,
        BigDecimal escrowBalanceMxc,
        BigDecimal availableBalanceMxc,
        BigDecimal earnedFromMentoringMxc,
        BigDecimal earnedFromFreelanceMxc,
        BigDecimal earnedFromCoursesMxc,
        Integer proposalsSent,
        Integer proposalsAccepted,
        Integer contractsActive,
        Integer contractsCompleted,
        Integer courseEnrollments
) {}