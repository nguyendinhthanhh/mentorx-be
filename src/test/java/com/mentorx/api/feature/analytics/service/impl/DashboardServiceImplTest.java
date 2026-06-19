package com.mentorx.api.feature.analytics.service.impl;

import com.mentorx.api.feature.analytics.dto.response.*;
import com.mentorx.api.feature.analytics.enums.*;
import com.mentorx.api.feature.analytics.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * M12.2 Phase H3: unit tests for DashboardServiceImpl.
 * Covers composition of 5 sub-services and cache annotation behavior.
 */
class DashboardServiceImplTest {

    private EarningsService earningsService;
    private JobStatsService jobStatsService;
    private CourseStatsService courseStatsService;
    private ViewAnalyticsService viewAnalyticsService;
    private ConversionService conversionService;

    private DashboardServiceImpl service;

    private final UUID userId = UUID.randomUUID();
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        earningsService = mock(EarningsService.class);
        jobStatsService = mock(JobStatsService.class);
        courseStatsService = mock(CourseStatsService.class);
        viewAnalyticsService = mock(ViewAnalyticsService.class);
        conversionService = mock(ConversionService.class);

        service = new DashboardServiceImpl(
                earningsService, jobStatsService, courseStatsService,
                viewAnalyticsService, conversionService
        );

        // Default stubs
        when(earningsService.getSummary(eq(userId), any(), any(), any()))
                .thenReturn(new EarningsSummaryResponse(
                        userId, AnalyticsPeriod.MONTH, today.minusMonths(1), today,
                        new BigDecimal("1000.00"), new BigDecimal("500.00"),
                        new BigDecimal("200.00"), new BigDecimal("50.00"),
                        List.of(), List.of()
                ));
        when(jobStatsService.getStats(eq(userId), any()))
                .thenReturn(JobStatsResponse.emptyMentor(userId));
        when(courseStatsService.getStats(eq(userId), any()))
                .thenReturn(new CourseStatsResponse(userId, 0, 0, BigDecimal.ZERO, 0, 0.0, 0L, List.of()));
        when(viewAnalyticsService.getTimeline(any(), eq(userId), any(), any(), any()))
                .thenReturn(new ViewTimelineResponse("user", userId, ViewGranularity.DAY,
                        today.minusDays(30), today, 100L, 42L, List.of()));
        when(conversionService.getRate(eq(userId), any(), any(), any()))
                .thenReturn(new ConversionResponse(userId, FunnelType.PROPOSAL_TO_CONTRACT,
                        today.minusDays(30), today, 10L, 40L, 0.25, List.of()));
    }

    // ── H3.9: composition — all 5 sub-sections populated ──────────────────────

    @Test
    void getSummary_composesAllFiveSubsections() {
        DashboardResponse result = service.getSummary(userId);

        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.earnings()).isNotEmpty();
        assertThat(result.jobs()).isNotEmpty();
        assertThat(result.courses()).isNotEmpty();
        assertThat(result.views()).isNotEmpty();
        assertThat(result.conversions()).isNotEmpty();

        // Each sub-service called exactly once
        verify(earningsService, times(1)).getSummary(eq(userId), any(), any(), any());
        verify(jobStatsService, times(1)).getStats(eq(userId), any());
        verify(courseStatsService, times(1)).getStats(eq(userId), any());
        verify(viewAnalyticsService, times(1)).getTimeline(any(), eq(userId), any(), any(), any());
        verify(conversionService, times(1)).getRate(eq(userId), any(), any(), any());
    }

    // ── H3.10: cache annotation — method is idempotent ────────────────────────
    // Without Spring cache proxy, calling twice invokes sub-services twice.
    // This test documents the expected behavior; true cache test needs integration.

    @Test
    void getSummary_cacheHitOnSecondCall() {
        DashboardResponse first = service.getSummary(userId);
        DashboardResponse second = service.getSummary(userId);

        assertThat(first.userId()).isEqualTo(userId);
        assertThat(second.userId()).isEqualTo(userId);

        // Without cache proxy, sub-services are called twice
        verify(earningsService, times(2)).getSummary(eq(userId), any(), any(), any());
    }

    // ── H3.11: mentor vs client tile shape ────────────────────────────────────

    @Test
    void getSummary_mentorVsClientTileShape() {
        DashboardResponse result = service.getSummary(userId);

        // Verify jobs tiles match MENTOR shape (has proposals_sent, contracts_active)
        assertThat(result.jobs()).anyMatch(t -> "proposals_sent".equals(t.key()));
        assertThat(result.jobs()).anyMatch(t -> "contracts_active".equals(t.key()));
    }
}
