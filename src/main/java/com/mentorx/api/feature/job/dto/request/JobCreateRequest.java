package com.mentorx.api.feature.job.dto.request;

import com.mentorx.api.common.enums.BudgetType;
import com.mentorx.api.common.enums.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record JobCreateRequest(
        @NotNull UUID clientId,
        Integer categoryId,
        @NotNull JobType jobType,
        @NotBlank String title,
        @NotBlank String description,
        @NotNull BudgetType budgetType,
        BigDecimal budgetMinMxc,
        BigDecimal budgetMaxMxc,
        BigDecimal hourlyRateMxc,
        BigDecimal estimatedHours,
        LocalDateTime deadlineAt,
        String attachmentUrl,
        List<String> attachments
) {}
