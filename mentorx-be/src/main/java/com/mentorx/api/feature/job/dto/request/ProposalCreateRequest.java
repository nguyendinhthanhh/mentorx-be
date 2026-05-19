package com.mentorx.api.feature.job.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProposalCreateRequest(
        @NotNull UUID jobId,
        @NotNull UUID mentorId,
        @NotBlank @Size(max = 5000) String coverLetter,
        @NotNull @DecimalMin("0.0") BigDecimal proposedAmount,
        @DecimalMin("0.0") BigDecimal proposedHourlyRate,
        Integer estimatedDurationDays,
        LocalDate proposedStartDate,
        LocalDate proposedDeliveryDate,
        List<Map<String, Object>> proposedMilestones,
        @Size(max = 2000) String relevantExperience,
        List<String> portfolioLinks,
        List<String> attachments,
        @Size(max = 1000) String questions,
        @Size(max = 1000) String terms
) {}
