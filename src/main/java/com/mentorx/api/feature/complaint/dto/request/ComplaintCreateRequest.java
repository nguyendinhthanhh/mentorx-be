package com.mentorx.api.feature.complaint.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ComplaintCreateRequest(
    @NotNull(message = "Complainant ID is required")
    UUID complainantId,

    @NotNull(message = "Respondent ID is required")
    UUID respondentId,

    UUID sessionId,

    UUID bookingId,

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    String title,

    @NotBlank(message = "Description is required")
    @Size(max = 2000)
    String description,

    @NotBlank(message = "Complaint category is required")
    String complaintCategory,

    Integer priorityLevel
) {}
