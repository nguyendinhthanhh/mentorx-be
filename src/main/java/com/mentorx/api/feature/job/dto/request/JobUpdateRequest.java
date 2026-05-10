package com.mentorx.api.feature.job.dto.request;

import com.mentorx.api.common.enums.BudgetType;
import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record JobUpdateRequest(
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
        String attachmentUrl,
        List<String> attachments
) {}
