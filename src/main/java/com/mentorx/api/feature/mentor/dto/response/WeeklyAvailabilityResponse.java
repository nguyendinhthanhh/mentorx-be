package com.mentorx.api.feature.mentor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAvailabilityResponse {

    private Map<Integer, List<TimeSlot>> weeklySchedule;
    private List<LocalDate> blockedDates;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
