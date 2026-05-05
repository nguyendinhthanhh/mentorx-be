package com.mentorx.api.feature.review.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewReportResponse(
        UUID id,
        UUID reviewId,
        UUID reporterId,
        String reporterName,
        String reportReason,
        String description,
        String status,
        LocalDateTime reviewedAt,
        UUID reviewedByAdminId,
        String actionTaken,
        String reviewNotes,
        LocalDateTime resolvedAt,
        Boolean isUpheld,
        Integer priorityLevel,
        Boolean isDuplicate,
        UUID originalReportId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
