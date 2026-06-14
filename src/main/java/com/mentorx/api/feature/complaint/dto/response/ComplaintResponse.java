package com.mentorx.api.feature.complaint.dto.response;

import com.mentorx.api.feature.complaint.enums.ComplaintOutcome;
import com.mentorx.api.feature.complaint.enums.ComplaintStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ComplaintResponse(
    UUID id,
    UUID complainantId,
    UUID respondentId,
    UUID sessionId,
    UUID bookingId,
    String title,
    String description,
    String complaintCategory,
    ComplaintStatus status,
    Integer priorityLevel,
    UUID mediatorId,
    LocalDateTime mediatorAssignedAt,
    LocalDateTime respondentNotifiedAt,
    LocalDateTime respondentRespondedAt,
    String respondentResponse,
    LocalDateTime responseDeadline,
    LocalDateTime mediationStartedAt,
    LocalDateTime resolvedAt,
    ComplaintOutcome outcome,
    String resolutionDetails,
    Double resolutionTimeHours,
    Boolean slaMet,
    List<ComplaintEvidenceResponse> evidence,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
