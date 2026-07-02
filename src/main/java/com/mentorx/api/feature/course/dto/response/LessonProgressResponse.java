package com.mentorx.api.feature.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonProgressResponse {

    private UUID enrollmentId;
    private UUID lessonId;
    private String lessonTitle;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private Integer watchDurationSec;
    private Integer progressPercent;
    private Integer scrollPercent;
    private Integer activeTimeSec;
    private Integer lastPositionSec;
    private Boolean completedByRule;
}
