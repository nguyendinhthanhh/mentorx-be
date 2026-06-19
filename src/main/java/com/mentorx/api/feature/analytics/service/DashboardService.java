package com.mentorx.api.feature.analytics.service;

import com.mentorx.api.feature.analytics.dto.response.DashboardResponse;

import java.util.UUID;

public interface DashboardService {
    DashboardResponse getSummary(UUID userId);
}
