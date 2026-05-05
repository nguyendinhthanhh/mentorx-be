package com.mentorx.api.feature.job.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MilestoneCreateRequest(
        @NotNull UUID contractId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @NotNull @Min(1) Integer milestoneOrder,
        @NotNull @DecimalMin("0.0") BigDecimal amount,
        LocalDate dueDate,
        Integer maxRevisions,
        Boolean isOptional,
        Boolean dependsOnPrevious
) {}
