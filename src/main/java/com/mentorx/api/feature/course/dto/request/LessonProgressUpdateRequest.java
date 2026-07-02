package com.mentorx.api.feature.course.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonProgressUpdateRequest {

    private Boolean isCompleted;

    @Min(value = 0, message = "Watch duration must be at least 0")
    private Integer watchDurationSec;

    @Min(value = 0, message = "Progress percent must be at least 0")
    private Integer progressPercent;

    @Min(value = 0, message = "Scroll percent must be at least 0")
    private Integer scrollPercent;

    @Min(value = 0, message = "Active time must be at least 0")
    private Integer activeTimeSec;

    @Min(value = 0, message = "Last video position must be at least 0")
    private Integer lastPositionSec;
}
