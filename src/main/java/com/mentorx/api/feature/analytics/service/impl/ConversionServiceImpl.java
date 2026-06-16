package com.mentorx.api.feature.analytics.service.impl;

import com.mentorx.api.feature.analytics.dto.response.ConversionResponse;
import com.mentorx.api.feature.analytics.dto.response.ConversionResponse.TrendPoint;
import com.mentorx.api.feature.analytics.enums.FunnelType;
import com.mentorx.api.feature.analytics.repository.UserInteractionEventRepository;
import com.mentorx.api.feature.analytics.service.ConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class ConversionServiceImpl implements ConversionService {

    private final UserInteractionEventRepository eventRepository;

    @Override
    public ConversionResponse getRate(UUID userId, FunnelType funnelType, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (endDate == null) endDate = today;
        if (startDate == null) startDate = endDate.minusDays(30);
        if (startDate.isAfter(endDate)) startDate = endDate.minusDays(1);

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = endDate.plusDays(1).atStartOfDay();

        FunnelSpec spec = resolveSpec(funnelType);

        long numerator = eventRepository.countByUserIdAndInteractionTypeAndInteractionTimestampBetween(
                userId, spec.numeratorType(), startDt, endDt);
        long denominator = eventRepository.countByUserIdAndInteractionTypeAndInteractionTimestampBetween(
                userId, spec.denominatorType(), startDt, endDt);

        double rate = denominator == 0 ? 0.0 :
                BigDecimal.valueOf(numerator)
                        .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP)
                        .doubleValue();

        List<TrendPoint> trend = buildTrend(userId, spec, startDt, endDt);

        return new ConversionResponse(
                userId, funnelType, startDate, endDate,
                numerator, denominator, rate, trend
        );
    }

    private List<TrendPoint> buildTrend(UUID userId, FunnelSpec spec, LocalDateTime start, LocalDateTime end) {
        Map<LocalDate, long[]> byDay = new TreeMap<>();
        Object[][] numRows = eventRepository.countByUserIdAndTypeGroupedByDay(
                userId, spec.numeratorType(), start, end);
        Object[][] denRows = eventRepository.countByUserIdAndTypeGroupedByDay(
                userId, spec.denominatorType(), start, end);

        for (Object[] row : denRows) {
            LocalDate d = ((java.sql.Date) row[0]).toLocalDate();
            byDay.computeIfAbsent(d, k -> new long[]{0, 0})[1] = ((Number) row[1]).longValue();
        }
        for (Object[] row : numRows) {
            LocalDate d = ((java.sql.Date) row[0]).toLocalDate();
            byDay.computeIfAbsent(d, k -> new long[]{0, 0})[0] = ((Number) row[1]).longValue();
        }

        List<TrendPoint> result = new ArrayList<>(byDay.size());
        for (Map.Entry<LocalDate, long[]> e : byDay.entrySet()) {
            long num = e.getValue()[0];
            long den = e.getValue()[1];
            double dayRate = den == 0 ? 0.0 :
                    BigDecimal.valueOf(num)
                            .divide(BigDecimal.valueOf(den), 4, RoundingMode.HALF_UP)
                            .doubleValue();
            result.add(new TrendPoint(e.getKey(), num, den, dayRate));
        }
        return result;
    }

    /**
     * Maps funnel types to interaction event type pairs.
     * DEC-005 (Option A): reuse {@code user_interaction_events} for funnel-step events.
     * The names below match the convention used by the chat / job / course modules —
     * see {@code matching/enums/InteractionEventType} for the canonical list.
     */
    private FunnelSpec resolveSpec(FunnelType type) {
        return switch (type) {
            case VIEW_TO_MESSAGE -> new FunnelSpec("CHAT_OPENED", "PROFILE_VIEW");
            case PROPOSAL_TO_CONTRACT -> new FunnelSpec("CONTRACT_SIGNED", "PROPOSAL_SUBMITTED");
            case VIEW_TO_PURCHASE -> new FunnelSpec("COURSE_PURCHASED", "COURSE_VIEW");
            case CHAT_TO_DEAL -> new FunnelSpec("CONTRACT_SIGNED", "CHAT_OPENED");
        };
    }

    private record FunnelSpec(String numeratorType, String denominatorType) {}
}
