package com.mentorx.api.feature.moderation.dto.response;

import com.mentorx.api.feature.moderation.enums.DisputeOutcome;
import com.mentorx.api.feature.moderation.enums.DisputeStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DisputeResponse(
        UUID id,
        UUID initiatorId,
        String initiatorName,
        UUID respondentId,
        String respondentName,
        UUID contractId,
        UUID jobId,
        String title,
        String description,
        String disputeCategory,
        DisputeStatus status,
        Integer priorityLevel,
        BigDecimal disputedAmountMxc,
        BigDecimal refundRequestedMxc,
        UUID mediatorId,
        LocalDateTime mediatorAssignedAt,
        LocalDateTime respondentNotifiedAt,
        LocalDateTime respondentRespondedAt,
        String respondentResponse,
        LocalDateTime responseDeadline,
        LocalDateTime mediationStartedAt,
        LocalDateTime resolvedAt,
        DisputeOutcome outcome,
        String resolutionDetails,
        BigDecimal refundAmountMxc,
        Boolean fundsInEscrow,
        UUID escrowRecordId,
        List<String> evidenceUrls,
        Integer initiatorEvidenceCount,
        Integer respondentEvidenceCount,
        Boolean requiresArbitration,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
