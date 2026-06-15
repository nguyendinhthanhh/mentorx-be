package com.mentorx.api.feature.analytics.dto.response;

import com.mentorx.api.feature.analytics.enums.FunnelType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ConversionResponse(
        UUID userId,
        FunnelType funnelType,
        LocalDate startDate,
        LocalDate endDate,
        long numerator,
        long denominator,
        double rate,
        List<TrendPoint> trend
) {
    public record TrendPoint(
            LocalDate date,
            long numerator,
            long denominator,
            double rate
    ) {}
}
