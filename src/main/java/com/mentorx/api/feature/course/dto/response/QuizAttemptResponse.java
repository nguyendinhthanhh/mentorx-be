package com.mentorx.api.feature.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptResponse {
    private UUID id;
    private UUID enrollmentId;
    private UUID lessonId;
    private BigDecimal score;
    private BigDecimal maxScore;
    private Boolean passed;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
