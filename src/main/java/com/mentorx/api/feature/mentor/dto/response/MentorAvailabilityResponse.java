package com.mentorx.api.feature.mentor.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record MentorAvailabilityResponse(
        UUID id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String timezone,
        Boolean isAvailable,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}