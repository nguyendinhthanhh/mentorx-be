package com.mentorx.api.feature.analytics.service;

import com.mentorx.api.feature.analytics.dto.response.JobStatsResponse;
import com.mentorx.api.feature.analytics.enums.JobStatsRole;

import java.util.UUID;

public interface JobStatsService {
    JobStatsResponse getStats(UUID userId, JobStatsRole role);
}
