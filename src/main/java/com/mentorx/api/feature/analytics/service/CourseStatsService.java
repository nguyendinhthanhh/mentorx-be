package com.mentorx.api.feature.analytics.service;

import com.mentorx.api.feature.analytics.dto.response.CourseStatsResponse;

import java.util.UUID;

public interface CourseStatsService {
    CourseStatsResponse getStats(UUID userId, UUID courseId);
}
