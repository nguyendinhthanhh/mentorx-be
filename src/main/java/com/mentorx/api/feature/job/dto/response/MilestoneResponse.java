package com.mentorx.api.feature.job.dto.response;

import com.mentorx.api.feature.job.enums.MilestoneStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MilestoneResponse(
        UUID id,
        UUID contractId,
        String contractTitle,
        String title,
        String description,
        Integer milestoneOrder,
        MilestoneStatus status,
        BigDecimal amount,
        LocalDate dueDate,
        LocalDateTime startedAt,
        LocalDateTime submittedAt,
        LocalDateTime approvedAt,
        LocalDateTime completedAt,
        String submissionNotes,
        String reviewNotes,
        Integer revisionCount,
        Integer maxRevisions,
        Boolean paymentReleased,
        LocalDateTime paymentReleasedAt,
        Long paymentTransactionId,
        Boolean isOptional,
        Boolean dependsOnPrevious,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
