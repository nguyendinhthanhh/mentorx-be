package com.mentorx.api.feature.analytics.service;

import com.mentorx.api.feature.analytics.dto.response.EarningsSummaryResponse;
import com.mentorx.api.feature.analytics.enums.AnalyticsPeriod;

import java.time.LocalDate;
import java.util.UUID;

public interface EarningsService {
    EarningsSummaryResponse getSummary(UUID userId, AnalyticsPeriod period, LocalDate startDate, LocalDate endDate);
}
