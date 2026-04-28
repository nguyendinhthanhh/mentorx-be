package com.mentorx.api.feature.mentor.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record MentorAvailabilityRequest(
        @NotNull(message = "Day of week is required")
        DayOfWeek dayOfWeek,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        @NotNull(message = "Timezone is required")
        @Size(max = 50, message = "Timezone must not exceed 50 characters")
        String timezone,

        Boolean isAvailable
) {
}