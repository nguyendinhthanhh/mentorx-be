package com.mentorx.api.feature.analytics.service;

import com.mentorx.api.feature.analytics.dto.response.ViewTimelineResponse;
import com.mentorx.api.feature.analytics.enums.ViewGranularity;

import java.time.LocalDate;
import java.util.UUID;

public interface ViewAnalyticsService {
    ViewTimelineResponse getTimeline(String targetType, UUID targetId, ViewGranularity granularity,
                                     LocalDate startDate, LocalDate endDate);
}
