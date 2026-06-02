package com.mentorx.api.feature.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChatConversationResolveRequest(
        @NotNull(message = "Recipient ID is required") UUID recipientId,
        @NotBlank(message = "Context type is required") String contextType,
        @NotNull(message = "Context ID is required") UUID contextId
) {}
