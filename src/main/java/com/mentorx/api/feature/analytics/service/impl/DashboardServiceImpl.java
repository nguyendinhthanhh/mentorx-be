package com.mentorx.api.feature.analytics.service.impl;

import com.mentorx.api.feature.analytics.dto.response.ConversionResponse;
import com.mentorx.api.feature.analytics.dto.response.CourseStatsResponse;
import com.mentorx.api.feature.analytics.dto.response.DashboardResponse;
import com.mentorx.api.feature.analytics.dto.response.DashboardResponse.Tile;
import com.mentorx.api.feature.analytics.dto.response.EarningsSummaryResponse;
import com.mentorx.api.feature.analytics.dto.response.JobStatsResponse;
import com.mentorx.api.feature.analytics.dto.response.ViewTimelineResponse;
import com.mentorx.api.feature.analytics.enums.AnalyticsPeriod;
import com.mentorx.api.feature.analytics.enums.FunnelType;
import com.mentorx.api.feature.analytics.enums.JobStatsRole;
import com.mentorx.api.feature.analytics.enums.ViewGranularity;
import com.mentorx.api.feature.analytics.service.ConversionService;
import com.mentorx.api.feature.analytics.service.CourseStatsService;
import com.mentorx.api.feature.analytics.service.DashboardService;
import com.mentorx.api.feature.analytics.service.EarningsService;
import com.mentorx.api.feature.analytics.service.JobStatsService;
import com.mentorx.api.feature.analytics.service.ViewAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final EarningsService earningsService;
    private final JobStatsService jobStatsService;
    private final CourseStatsService courseStatsService;
    private final ViewAnalyticsService viewAnalyticsService;
    private final ConversionService conversionService;

    /**
     * Cached 5 minutes (per Phase 7 task 7.4) to avoid re-running the heavy
     * sub-aggregations on every dashboard load. Cache name {@code dashboard}
     * is defined in {@code application.yml}.
     */
    @Override
    @Cacheable(cacheNames = "dashboard", key = "#userId.toString()")
    public DashboardResponse getSummary(UUID userId) {
        LocalDate today = LocalDate.now();
        EarningsSummaryResponse earnings = earningsService.getSummary(
                userId, AnalyticsPeriod.MONTH, today.minusMonths(1), today);
        JobStatsResponse jobs = jobStatsService.getStats(userId, JobStatsRole.MENTOR);
        CourseStatsResponse courses = courseStatsService.getStats(userId, null);
        ViewTimelineResponse views = viewAnalyticsService.getTimeline(
                "user", userId, ViewGranularity.DAY, today.minusDays(30), today);
        ConversionResponse conversion = conversionService.getRate(
                userId, FunnelType.PROPOSAL_TO_CONTRACT, today.minusDays(30), today);

        return new DashboardResponse(
                userId, today,
                buildEarningsTiles(earnings),
                buildJobsTiles(jobs),
                buildCoursesTiles(courses),
                buildViewsTiles(views),
                buildConversionTiles(conversion)
        );
    }

    private List<Tile> buildEarningsTiles(EarningsSummaryResponse e) {
        return List.of(
                new Tile("total_earned",   "Total earned (30d)",    e.totalEarnedMxc(),   "MXC",  null),
                new Tile("available",      "Available balance",     e.availableBalanceMxc(), "MXC", null),
                new Tile("escrow",         "Escrow balance",        e.escrowBalanceMxc(), "MXC",  null),
                new Tile("withdrawn",      "Withdrawn (30d)",       e.withdrawnMxc(),      "MXC",  null)
        );
    }

    private List<Tile> buildJobsTiles(JobStatsResponse j) {
        if (j.role() == JobStatsRole.MENTOR) {
            return List.of(
                    new Tile("proposals_sent",     "Proposals sent",     j.proposalsSent(),     "count", null),
                    new Tile("proposals_accepted", "Proposals accepted", j.proposalsAccepted(), "count", null),
                    new Tile("acceptance_rate",    "Acceptance rate",    j.proposalAcceptanceRate(), "ratio", null),
                    new Tile("contracts_active",   "Active contracts",   j.contractsActive(),   "count", null),
                    new Tile("contracts_completed","Completed contracts",j.contractsCompleted(),"count", null)
            );
        }
        return List.of(
                new Tile("jobs_posted",     "Jobs posted",         j.jobsPosted(),         "count", null),
                new Tile("jobs_open",       "Open jobs",           j.jobsOpen(),           "count", null),
                new Tile("jobs_in_progress","In progress",         j.jobsInProgress(),     "count", null),
                new Tile("proposals_received","Proposals received", j.totalProposalsReceived(), "count", null)
        );
    }

    private List<Tile> buildCoursesTiles(CourseStatsResponse c) {
        return List.of(
                new Tile("courses_total",  "Total courses",    c.totalCourses(),        "count", null),
                new Tile("courses_sold",   "Courses sold",     c.totalCoursesSold(),    "count", null),
                new Tile("revenue",        "Total revenue",    c.totalRevenueMxc(),     "MXC",   null),
                new Tile("enrollments",    "Enrollments",      c.totalEnrollments(),    "count", null),
                new Tile("lesson_views",   "Lesson views",     c.totalLessonViews(),    "count", null)
        );
    }

    private List<Tile> buildViewsTiles(ViewTimelineResponse v) {
        return List.of(
                new Tile("total_views",    "Total views (30d)", v.totalViews(),     "count", null),
                new Tile("unique_viewers", "Unique viewers",    v.uniqueViewers(),  "count", null),
                new Tile("granularity",    "Bucket",            v.granularity().name(), "enum", null)
        );
    }

    private List<Tile> buildConversionTiles(ConversionResponse c) {
        return List.of(
                new Tile("funnel",         "Funnel",       c.funnelType().name(),         "enum",   null),
                new Tile("numerator",      "Conversions",  c.numerator(),                "count",  null),
                new Tile("denominator",    "Top of funnel",c.denominator(),              "count",  null),
                new Tile("rate",           "Conversion rate", c.rate(),                   "ratio",  null)
        );
    }
}
