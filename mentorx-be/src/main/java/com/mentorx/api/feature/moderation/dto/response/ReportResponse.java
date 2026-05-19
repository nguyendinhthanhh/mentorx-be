package com.mentorx.api.feature.moderation.dto.response;

import com.mentorx.api.feature.moderation.enums.ReportStatus;
import com.mentorx.api.feature.moderation.enums.ReportTargetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        UUID reporterId,
        String reporterName,
        ReportTargetType targetType,
        UUID targetId,
        UUID reportedUserId,
        String reportedUserName,
        String reportCategory,
        String reason,
        ReportStatus status,
        Integer priorityLevel,
        UUID assignedToAdminId,
        LocalDateTime assignedAt,
        LocalDateTime reviewedAt,
        LocalDateTime resolvedAt,
        String actionTaken,
        String moderatorNotes,
        Boolean isUpheld,
        Boolean isDuplicate,
        UUID originalReportId,
        Integer similarReportCount,
        Boolean isUrgent,
        Boolean contentHidden,
        LocalDateTime contentHiddenAt,
        List<String> evidenceUrls,
        String reportContext,
        Integer escalationLevel,
        LocalDateTime escalatedAt,
        String escalationReason,
        LocalDateTime slaDeadline,
        Boolean slaMet,
        BigDecimal resolutionTimeHours,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
