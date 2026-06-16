package com.mentorx.api.feature.analytics.service.impl;

import com.mentorx.api.feature.analytics.dto.response.EarningsSummaryResponse;
import com.mentorx.api.feature.analytics.dto.response.EarningsSummaryResponse.BySource;
import com.mentorx.api.feature.analytics.dto.response.EarningsSummaryResponse.TimelinePoint;
import com.mentorx.api.feature.analytics.entity.EarningsDailySnapshot;
import com.mentorx.api.feature.analytics.enums.AnalyticsPeriod;
import com.mentorx.api.feature.analytics.enums.EarningsSource;
import com.mentorx.api.feature.analytics.repository.EarningsDailySnapshotRepository;
import com.mentorx.api.feature.analytics.service.EarningsService;
import com.mentorx.api.feature.wallet.repository.WalletRepository;
import com.mentorx.api.common.enums.WalletAccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EarningsServiceImpl implements EarningsService {

    private final EarningsDailySnapshotRepository snapshotRepository;
    private final WalletRepository walletRepository;

    @Override
    public EarningsSummaryResponse getSummary(UUID userId, AnalyticsPeriod period,
                                              LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (endDate == null) {
            endDate = today;
        }
        if (startDate == null) {
            startDate = inferDefaultStart(period, endDate);
        }
        if (startDate.isAfter(endDate)) {
            startDate = endDate.minusDays(1);
        }

        List<EarningsDailySnapshot> rows = snapshotRepository
                .findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(userId, startDate, endDate);

        BigDecimal totalEarned = sum(rows, EarningsDailySnapshot::getEarnedMxc);
        BigDecimal available = walletRepository
                .findByUserIdAndAccountType(userId, WalletAccountType.USER_AVAILABLE)
                .map(w -> w.getBalanceMxc())
                .orElse(BigDecimal.ZERO);
        BigDecimal escrow = walletRepository
                .findByUserIdAndAccountType(userId, WalletAccountType.USER_PENDING)
                .map(w -> w.getBalanceMxc())
                .orElse(BigDecimal.ZERO);
        BigDecimal withdrawn = sum(rows, EarningsDailySnapshot::getWithdrawnMxc);

        List<BySource> bySource = List.of(
                new BySource(
                        EarningsSource.LONG_TERM_MENTORING,
                        sum(rows, EarningsDailySnapshot::getEarnedFromMentoringMxc),
                        sumInt(rows, EarningsDailySnapshot::getProposalsAccepted),
                        sumInt(rows, EarningsDailySnapshot::getContractsActive),
                        null, null
                ),
                new BySource(
                        EarningsSource.FREELANCE_PROJECT,
                        sum(rows, EarningsDailySnapshot::getEarnedFromFreelanceMxc),
                        sumInt(rows, EarningsDailySnapshot::getProposalsAccepted),
                        sumInt(rows, EarningsDailySnapshot::getContractsActive),
                        null, null
                ),
                new BySource(
                        EarningsSource.COURSE_SALE,
                        sum(rows, EarningsDailySnapshot::getEarnedFromCoursesMxc),
                        null, null,
                        sumInt(rows, EarningsDailySnapshot::getCoursesSold),
                        sumInt(rows, EarningsDailySnapshot::getCourseEnrollments)
                )
        );

        List<TimelinePoint> timeline = rollupTimeline(rows, period);

        return new EarningsSummaryResponse(
                userId, period, startDate, endDate,
                totalEarned, available, escrow, withdrawn,
                bySource, timeline
        );
    }

    private LocalDate inferDefaultStart(AnalyticsPeriod period, LocalDate endDate) {
        return switch (period) {
            case DAY -> endDate.minusDays(1);
            case WEEK -> endDate.minusWeeks(1);
            case MONTH -> endDate.minusMonths(1);
            case YEAR -> endDate.minusYears(1);
        };
    }

    /**
     * Aggregates daily rows into period buckets. The backend stores daily granularity;
     * server-side rollup keeps the response size bounded and the client fast.
     */
    private List<TimelinePoint> rollupTimeline(List<EarningsDailySnapshot> rows, AnalyticsPeriod period) {
        return rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        s -> bucketKey(s.getSnapshotDate(), period),
                        java.util.TreeMap::new,
                        java.util.stream.Collectors.reducing(
                                new TimelinePoint(java.time.LocalDate.MIN, BigDecimal.ZERO, 0, 0),
                                s -> new TimelinePoint(s.getSnapshotDate(),
                                        s.getEarnedMxc(),
                                        s.getJobsCompleted() == null ? 0 : s.getJobsCompleted(),
                                        s.getCoursesSold() == null ? 0 : s.getCoursesSold()),
                                (a, b) -> new TimelinePoint(
                                        a.date().isBefore(b.date()) ? a.date() : b.date(),
                                        a.earnedMxc().add(b.earnedMxc()),
                                        (a.jobsCompleted() == null ? 0 : a.jobsCompleted())
                                                + (b.jobsCompleted() == null ? 0 : b.jobsCompleted()),
                                        (a.coursesSold() == null ? 0 : a.coursesSold())
                                                + (b.coursesSold() == null ? 0 : b.coursesSold())
                                )
                        )
                ))
                .entrySet().stream()
                .map(e -> new TimelinePoint(
                        e.getKey(),
                        e.getValue().earnedMxc(),
                        e.getValue().jobsCompleted(),
                        e.getValue().coursesSold()))
                .toList();
    }

    private LocalDate bucketKey(LocalDate date, AnalyticsPeriod period) {
        return switch (period) {
            case DAY -> date;
            case WEEK -> {
                // ISO week — anchor to the Monday of the date's week.
                LocalDate monday = date;
                while (monday.getDayOfWeek().getValue() != 1) {
                    monday = monday.minusDays(1);
                }
                yield monday;
            }
            case MONTH -> date.withDayOfMonth(1);
            case YEAR -> date.withDayOfYear(1);
        };
    }

    private BigDecimal sum(List<EarningsDailySnapshot> rows,
                           java.util.function.Function<EarningsDailySnapshot, BigDecimal> f) {
        return rows.stream()
                .map(f)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer sumInt(List<EarningsDailySnapshot> rows,
                           java.util.function.Function<EarningsDailySnapshot, Integer> f) {
        return rows.stream()
                .map(f)
                .filter(java.util.Objects::nonNull)
                .reduce(0, Integer::sum);
    }
}
