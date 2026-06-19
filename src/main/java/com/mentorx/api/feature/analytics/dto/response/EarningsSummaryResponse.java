package com.mentorx.api.feature.analytics.dto.response;

import com.mentorx.api.feature.analytics.enums.AnalyticsPeriod;
import com.mentorx.api.feature.analytics.enums.EarningsSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EarningsSummaryResponse(
        UUID userId,
        AnalyticsPeriod period,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalEarnedMxc,
        BigDecimal availableBalanceMxc,
        BigDecimal escrowBalanceMxc,
        BigDecimal withdrawnMxc,
        List<BySource> bySource,
        List<TimelinePoint> timeline
) {
    public record BySource(
            EarningsSource source,
            BigDecimal earnedMxc,
            Integer proposalsAccepted,
            Integer contractsActive,
            Integer coursesSold,
            Integer enrollments
    ) {}

    public record TimelinePoint(
            LocalDate date,
            BigDecimal earnedMxc,
            Integer jobsCompleted,
            Integer coursesSold
    ) {}
}
