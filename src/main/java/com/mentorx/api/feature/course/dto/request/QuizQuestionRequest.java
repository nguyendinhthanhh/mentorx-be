package com.mentorx.api.feature.course.dto.request;

import com.mentorx.api.feature.course.enums.QuizQuestionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionRequest {
    @NotNull
    private QuizQuestionType questionType;

    @NotBlank
    private String questionText;

    @NotBlank
    private String answerDataJson;

    @Min(1)
    private Integer points;

    private String explanation;

    @Min(1)
    private Integer orderIndex;
}
