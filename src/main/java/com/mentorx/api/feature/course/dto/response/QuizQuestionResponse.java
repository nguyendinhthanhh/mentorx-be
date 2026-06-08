package com.mentorx.api.feature.course.dto.response;

import com.mentorx.api.feature.course.enums.QuizQuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionResponse {
    private UUID id;
    private UUID lessonId;
    private QuizQuestionType questionType;
    private String questionText;
    private String optionsJson;
    private String correctAnswersJson;
    private Integer points;
    private String explanation;
    private Integer orderIndex;
}
