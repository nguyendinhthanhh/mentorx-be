package com.mentorx.api.feature.analytics.service.impl;

import com.mentorx.api.feature.analytics.dto.response.ConversionResponse;
import com.mentorx.api.feature.analytics.enums.FunnelType;
import com.mentorx.api.feature.analytics.repository.UserInteractionEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConversionServiceImplTest {

    private UserInteractionEventRepository eventRepository;
    private ConversionServiceImpl service;

    @BeforeEach
    void setUp() {
        eventRepository = mock(UserInteractionEventRepository.class);
        service = new ConversionServiceImpl(eventRepository);
    }

    @Test
    void zeroDenominator_yieldsZeroRate() {
        UUID userId = UUID.randomUUID();
        when(eventRepository.countByUserIdAndInteractionTypeAndInteractionTimestampBetween(
                eq(userId), any(), any(), any())).thenReturn(0L);
        when(eventRepository.countByUserIdAndTypeGroupedByDay(
                eq(userId), any(), any(), any())).thenReturn(new Object[][]{});

        ConversionResponse response = service.getRate(userId, FunnelType.PROPOSAL_TO_CONTRACT, null, null);

        assertThat(response.numerator()).isZero();
        assertThat(response.denominator()).isZero();
        assertThat(response.rate()).isZero();
        assertThat(response.trend()).isEmpty();
        assertThat(response.funnelType()).isEqualTo(FunnelType.PROPOSAL_TO_CONTRACT);
    }

    @Test
    void knownNumeratorAndDenominator_computesRate() {
        UUID userId = UUID.randomUUID();
        when(eventRepository.countByUserIdAndInteractionTypeAndInteractionTimestampBetween(
                eq(userId), eq("CONTRACT_SIGNED"), any(), any())).thenReturn(5L);
        when(eventRepository.countByUserIdAndInteractionTypeAndInteractionTimestampBetween(
                eq(userId), eq("PROPOSAL_SUBMITTED"), any(), any())).thenReturn(20L);
        when(eventRepository.countByUserIdAndTypeGroupedByDay(
                eq(userId), any(), any(), any())).thenReturn(new Object[][]{});

        ConversionResponse response = service.getRate(userId, FunnelType.PROPOSAL_TO_CONTRACT, null, null);

        assertThat(response.numerator()).isEqualTo(5L);
        assertThat(response.denominator()).isEqualTo(20L);
        // 5/20 = 0.25
        assertThat(response.rate()).isEqualTo(0.25);
    }

    @Test
    void defaultDateRange_last30Days() {
        UUID userId = UUID.randomUUID();
        when(eventRepository.countByUserIdAndInteractionTypeAndInteractionTimestampBetween(
                eq(userId), any(), any(), any())).thenReturn(0L);
        when(eventRepository.countByUserIdAndTypeGroupedByDay(
                eq(userId), any(), any(), any())).thenReturn(new Object[][]{});

        LocalDate today = LocalDate.now();
        ConversionResponse response = service.getRate(userId, FunnelType.VIEW_TO_PURCHASE, null, null);

        assertThat(response.endDate()).isEqualTo(today);
        assertThat(response.startDate()).isEqualTo(today.minusDays(30));
    }
}
