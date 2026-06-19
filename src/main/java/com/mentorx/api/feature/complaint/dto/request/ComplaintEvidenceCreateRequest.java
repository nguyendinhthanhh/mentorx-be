package com.mentorx.api.feature.complaint.dto.request;

import com.mentorx.api.feature.complaint.enums.EvidenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComplaintEvidenceCreateRequest(
    @NotNull(message = "Evidence type is required")
    EvidenceType evidenceType,

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    String title,

    @Size(max = 1000)
    String description,

    String fileUrl,

    String filename,

    String mimeType,

    Long fileSize
) {}
