package com.mentorx.api.feature.analytics.service;

import com.mentorx.api.feature.analytics.dto.response.ConversionResponse;
import com.mentorx.api.feature.analytics.enums.FunnelType;

import java.time.LocalDate;
import java.util.UUID;

public interface ConversionService {
    ConversionResponse getRate(UUID userId, FunnelType funnelType, LocalDate startDate, LocalDate endDate);
}
