package com.mentorx.api.feature.analytics.service.impl;

import com.mentorx.api.feature.analytics.dto.response.ViewTimelineResponse;
import com.mentorx.api.feature.analytics.dto.response.ViewTimelineResponse.TimelineBucket;
import com.mentorx.api.feature.analytics.enums.ViewGranularity;
import com.mentorx.api.feature.analytics.repository.ViewEventRepository;
import com.mentorx.api.feature.analytics.service.ViewAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViewAnalyticsServiceImpl implements ViewAnalyticsService {

    private final ViewEventRepository viewEventRepository;

    @Override
    public ViewTimelineResponse getTimeline(String targetType, UUID targetId, ViewGranularity granularity,
                                            LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (endDate == null) endDate = today;
        if (startDate == null) startDate = endDate.minusDays(30);
        if (startDate.isAfter(endDate)) startDate = endDate.minusDays(1);
        if (granularity == null) granularity = ViewGranularity.DAY;

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Object[]> rows = viewEventRepository.aggregateByDay(targetType, targetId, start, end);

        Map<LocalDate, long[]> aggregated = new TreeMap<>();
        for (Object[] row : rows) {
            LocalDate bucket = toLocalDate(row[0]);
            long total = ((Number) row[1]).longValue();
            long unique = ((Number) row[2]).longValue();
            long[] existing = aggregated.computeIfAbsent(bucket, k -> new long[]{0, 0});
            existing[0] += total;
            existing[1] = Math.max(existing[1], unique);
        }

        Map<LocalDate, long[]> rolled = rollByGranularity(aggregated, granularity);

        long totalViews = rolled.values().stream().mapToLong(v -> v[0]).sum();
        long uniqueViewers = rolled.values().stream().mapToLong(v -> v[1]).max().orElse(0L);

        List<TimelineBucket> timeline = new ArrayList<>(rolled.size());
        for (Map.Entry<LocalDate, long[]> e : rolled.entrySet()) {
            timeline.add(new TimelineBucket(e.getKey(), e.getValue()[0], e.getValue()[1]));
        }

        return new ViewTimelineResponse(
                targetType, targetId, granularity, startDate, endDate,
                totalViews, uniqueViewers, timeline
        );
    }

    private Map<LocalDate, long[]> rollByGranularity(Map<LocalDate, long[]> perDay, ViewGranularity granularity) {
        if (granularity == ViewGranularity.DAY) {
            return perDay;
        }
        Map<LocalDate, long[]> out = new TreeMap<>();
        for (Map.Entry<LocalDate, long[]> e : perDay.entrySet()) {
            LocalDate key = switch (granularity) {
                case WEEK -> {
                    LocalDate d = e.getKey();
                    while (d.getDayOfWeek().getValue() != 1) {
                        d = d.minusDays(1);
                    }
                    yield d;
                }
                case MONTH -> e.getKey().withDayOfMonth(1);
                default -> e.getKey();
            };
            long[] existing = out.computeIfAbsent(key, k -> new long[]{0, 0});
            existing[0] += e.getValue()[0];
            existing[1] = Math.max(existing[1], e.getValue()[1]);
        }
        return out;
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }
        if (value instanceof LocalDate ld) {
            return ld;
        }
        throw new IllegalStateException("Unexpected date type from native query: " + value.getClass());
    }
}
