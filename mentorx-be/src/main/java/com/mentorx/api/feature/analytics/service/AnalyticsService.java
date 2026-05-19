package com.mentorx.api.feature.analytics.service;

import com.mentorx.api.feature.analytics.dto.request.ViewEventRequest;
import com.mentorx.api.feature.analytics.dto.response.EarningsSnapshotResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AnalyticsService {
    void recordView(ViewEventRequest request, UUID viewerId);
    long getViewCount(String targetType, UUID targetId);
    Page<EarningsSnapshotResponse> getUserEarningsSnapshots(UUID userId, Pageable pageable);
}
