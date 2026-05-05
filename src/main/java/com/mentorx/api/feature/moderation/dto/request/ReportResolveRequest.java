package com.mentorx.api.feature.moderation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportResolveRequest(
        @NotBlank(message = "Action taken is required") @Size(max = 50) String actionTaken,
        @Size(max = 2000) String moderatorNotes,
        @NotNull(message = "isUpheld must be specified") Boolean isUpheld
) {}
