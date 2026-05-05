package com.mentorx.api.feature.matching.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record SavedSearchRequest(
    @NotNull(message = "User ID is required")
    UUID userId,
    
    @NotBlank(message = "Search name is required")
    @Size(max = 100, message = "Search name must not exceed 100 characters")
    String name,
    
    @NotBlank(message = "Filters are required")
    String filters
) {}
