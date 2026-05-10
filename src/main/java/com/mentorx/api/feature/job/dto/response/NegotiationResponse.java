package com.mentorx.api.feature.job.dto.response;

import com.mentorx.api.feature.job.enums.NegotiationStatus;
import com.mentorx.api.feature.job.enums.SenderType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record NegotiationResponse(
        UUID id,
        UUID proposalId,
        UUID senderId,
        String senderName,
        SenderType senderType,
        String message,
        BigDecimal proposedAmount,
        BigDecimal proposedHourlyRate,
        Integer estimatedDurationDays,
        LocalDate proposedStartDate,
        LocalDate proposedDeliveryDate,
        NegotiationStatus status,
        LocalDateTime createdAt,
        LocalDateTime respondedAt
) {
}
