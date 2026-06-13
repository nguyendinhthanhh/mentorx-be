package com.mentorx.api.feature.complaint.dto.response;

import com.mentorx.api.feature.complaint.enums.EvidenceType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ComplaintEvidenceResponse(
    UUID id,
    UUID disputeId,
    UUID submittedByUserId,
    EvidenceType evidenceType,
    String title,
    String description,
    String fileUrl,
    String filename,
    String mimeType,
    Long fileSize,
    Boolean isReviewed,
    LocalDateTime reviewedAt,
    UUID reviewedByUserId,
    String reviewNotes,
    Boolean isFlagged,
    String flagReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
