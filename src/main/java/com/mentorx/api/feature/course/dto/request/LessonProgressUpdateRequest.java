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
}
