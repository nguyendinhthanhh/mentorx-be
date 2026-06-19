package com.mentorx.api.feature.analytics.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.common.util.SecurityUtils;
import com.mentorx.api.feature.analytics.dto.request.ViewEventRequest;
import com.mentorx.api.feature.analytics.dto.response.ConversionResponse;
import com.mentorx.api.feature.analytics.dto.response.CourseStatsResponse;
import com.mentorx.api.feature.analytics.dto.response.DashboardResponse;
import com.mentorx.api.feature.analytics.dto.response.EarningsSnapshotResponse;
import com.mentorx.api.feature.analytics.dto.response.EarningsSummaryResponse;
import com.mentorx.api.feature.analytics.dto.response.JobStatsResponse;
import com.mentorx.api.feature.analytics.dto.response.ViewTimelineResponse;
import com.mentorx.api.feature.analytics.enums.AnalyticsPeriod;
import com.mentorx.api.feature.analytics.enums.FunnelType;
import com.mentorx.api.feature.analytics.enums.JobStatsRole;
import com.mentorx.api.feature.analytics.enums.ViewGranularity;
import com.mentorx.api.feature.analytics.service.AnalyticsService;
import com.mentorx.api.feature.analytics.service.ConversionService;
import com.mentorx.api.feature.analytics.service.CourseStatsService;
import com.mentorx.api.feature.analytics.service.DashboardService;
import com.mentorx.api.feature.analytics.service.EarningsService;
import com.mentorx.api.feature.analytics.service.JobStatsService;
import com.mentorx.api.feature.analytics.service.ViewAnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final EarningsService earningsService;
    private final JobStatsService jobStatsService;
    private final CourseStatsService courseStatsService;
    private final ConversionService conversionService;
    private final ViewAnalyticsService viewAnalyticsService;
    private final DashboardService dashboardService;
    private final MentorModeAccessService accessService;

    // ── Phase 1: View events (public) ─────────────────────────────────────────

    @PostMapping("/views")
    public ResponseEntity<ApiResponse<Void>> recordView(
            @Valid @RequestBody ViewEventRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        UUID viewerId = SecurityUtils.getCurrentUserIdOrNull();
        // FE-DEC-005 Option B: resolve IP from request headers for anonymous dedup
        String ip = request.ipAddress();
        if (ip == null || ip.isBlank()) {
            ip = httpRequest.getHeader("X-Forwarded-For");
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim(); // first IP in chain = real client
            }
            if (ip == null || ip.isBlank()) {
                ip = httpRequest.getRemoteAddr();
            }
        }
        ViewEventRequest enriched = new ViewEventRequest(request.targetType(), request.targetId(), ip);
        analyticsService.recordView(enriched, viewerId);
        return ResponseEntity.ok(ApiResponse.success("View recorded successfully", null));
    }

    @GetMapping("/views/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getViewCount(
            @RequestParam String targetType,
            @RequestParam UUID targetId) {
        long count = analyticsService.getViewCount(targetType, targetId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("viewCount", count)));
    }

    // ── Phase 6: View timeline ────────────────────────────────────────────────

    @GetMapping("/views/timeline")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ViewTimelineResponse>> getViewTimeline(
            @RequestParam String targetType,
            @RequestParam UUID targetId,
            @RequestParam(defaultValue = "DAY") ViewGranularity granularity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // Owner-only for user-profile view timelines (DEC-007); other target types are non-PII.
        if ("user".equalsIgnoreCase(targetType)) {
            accessService.requireSelfOrAdmin(targetId);
        }
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(30);
        return ResponseEntity.ok(ApiResponse.success(
                viewAnalyticsService.getTimeline(targetType, targetId, granularity, start, end)));
    }

    // ── Phase 1: Earnings daily ───────────────────────────────────────────────

    @GetMapping("/earnings/daily")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<EarningsSnapshotResponse>>> getUserEarningsSnapshots(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        accessService.requireSelfOrAdmin(userId);
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getUserEarningsSnapshots(userId, PageRequest.of(page, size))));
    }

    // ── Phase 2: Earnings summary ─────────────────────────────────────────────

    @GetMapping("/earnings/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EarningsSummaryResponse>> getEarningsSummary(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "MONTH") AnalyticsPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        accessService.requireSelfOrAdmin(userId);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusMonths(1);
        return ResponseEntity.ok(ApiResponse.success(
                earningsService.getSummary(userId, period, start, end)));
    }

    // ── Phase 3: Job statistics ───────────────────────────────────────────────

    @GetMapping("/jobs/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<JobStatsResponse>> getJobStats(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "MENTOR") JobStatsRole role) {
        accessService.requireSelfOrAdmin(userId);
        return ResponseEntity.ok(ApiResponse.success(jobStatsService.getStats(userId, role)));
    }

    // ── Phase 4: Course sales statistics ──────────────────────────────────────

    @GetMapping("/courses/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseStatsResponse>> getCourseStats(
            @RequestParam UUID userId,
            @RequestParam(required = false) UUID courseId) {
        accessService.requireSelfOrAdmin(userId);
        return ResponseEntity.ok(ApiResponse.success(courseStatsService.getStats(userId, courseId)));
    }

    // ── Phase 5: Conversion rate ──────────────────────────────────────────────

    @GetMapping("/conversion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ConversionResponse>> getConversion(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "PROPOSAL_TO_CONTRACT") FunnelType funnelType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        accessService.requireSelfOrAdmin(userId);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(30);
        return ResponseEntity.ok(ApiResponse.success(
                conversionService.getRate(userId, funnelType, start, end)));
    }

    // ── Phase 7: Dashboard summary (cached) ───────────────────────────────────

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestParam UUID userId) {
        accessService.requireSelfOrAdmin(userId);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary(userId)));
    }
}
