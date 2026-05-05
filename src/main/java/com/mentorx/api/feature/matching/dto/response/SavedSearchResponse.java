package com.mentorx.api.feature.matching.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SavedSearchResponse(
    UUID id,
    UUID userId,
    String userFullName,
    String name,
    String filters,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt
) {}
