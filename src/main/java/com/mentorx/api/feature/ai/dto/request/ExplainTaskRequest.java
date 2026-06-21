package com.mentorx.api.feature.ai.dto.request;

import com.mentorx.api.feature.ai.enums.AiTaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ExplainTaskRequest(
    @NotNull AiTaskType taskType,
    @NotNull UUID taskId,
    @NotBlank String question
) {}
