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
        Short jobsCompleted,
        Short coursesSold
) {}
