package com.mentorx.api.feature.job.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record NegotiationRequest(
        @NotNull(message = "Proposal ID is required")
        UUID proposalId,
        
        @NotNull(message = "Sender ID is required")
        UUID senderId,
        
        @NotBlank(message = "Message is required")
        @Size(min = 20, max = 1000, message = "Message must be between 20 and 1000 characters")
        String message,
        
        BigDecimal proposedAmount,
        BigDecimal proposedHourlyRate,
        Integer estimatedDurationDays,
        LocalDateTime deadlineAt,
        @Size(max = 1000, message = "Scope description must be at most 1000 characters")
        String scopeDescription,
        LocalDate proposedStartDate,
        LocalDate proposedDeliveryDate
) {
}
