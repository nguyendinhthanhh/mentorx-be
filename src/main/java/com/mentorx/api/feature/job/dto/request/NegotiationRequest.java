package com.mentorx.api.feature.job.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record NegotiationRequest(
        @NotNull(message = "Proposal ID is required")
        UUID proposalId,
        
        @NotNull(message = "Sender ID is required")
        UUID senderId,
        
        @NotBlank(message = "Message is required")
        @Size(min = 10, max = 2000, message = "Message must be between 10 and 2000 characters")
        String message,
        
        BigDecimal proposedAmount,
        BigDecimal proposedHourlyRate,
        Integer estimatedDurationDays,
        LocalDate proposedStartDate,
        LocalDate proposedDeliveryDate
) {
}
