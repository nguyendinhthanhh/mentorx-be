package com.mentorx.api.feature.analytics.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.analytics.dto.request.ViewEventRequest;
import com.mentorx.api.feature.analytics.dto.response.EarningsSnapshotResponse;
import com.mentorx.api.feature.analytics.service.AnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/views")
    public ResponseEntity<ApiResponse<Void>> recordView(
            @Valid @RequestBody ViewEventRequest request,
            @RequestParam(required = false) UUID viewerId) {
        analyticsService.recordView(request, viewerId);
        return ResponseEntity.ok(ApiResponse.success("View recorded successfully", null));
    }

    @GetMapping("/views/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getViewCount(
            @RequestParam String targetType,
            @RequestParam UUID targetId) {
        long count = analyticsService.getViewCount(targetType, targetId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("viewCount", count)));
    }

    @GetMapping("/earnings/daily")
    public ResponseEntity<ApiResponse<Page<EarningsSnapshotResponse>>> getUserEarningsSnapshots(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getUserEarningsSnapshots(userId, PageRequest.of(page, size))));
    }
}
