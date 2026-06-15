package com.mentorx.api.feature.analytics.dto.response;

import com.mentorx.api.feature.analytics.enums.ViewGranularity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ViewTimelineResponse(
        String targetType,
        UUID targetId,
        ViewGranularity granularity,
        LocalDate startDate,
        LocalDate endDate,
        long totalViews,
        long uniqueViewers,
        List<TimelineBucket> timeline
) {
    public record TimelineBucket(
            LocalDate date,
            long views,
            long uniqueViewers
    ) {}
}
