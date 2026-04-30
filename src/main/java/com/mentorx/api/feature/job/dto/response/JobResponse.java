package com.mentorx.api.feature.job.dto.response;

import com.mentorx.api.common.enums.BudgetType;
import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record JobResponse(
        UUID id,
        UUID clientId,
        String clientName,
        Integer categoryId,
        JobType jobType,
        String title,
        String description,
        BudgetType budgetType,
        BigDecimal budgetMinMxc,
        BigDecimal budgetMaxMxc,
        BigDecimal hourlyRateMxc,
        BigDecimal estimatedHours,
        LocalDateTime deadlineAt,
        JobStatus status,
        Boolean isFeatured,
        Integer viewCount,
        Integer proposalCount,
        LocalDateTime publishedAt,
        LocalDateTime closedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
