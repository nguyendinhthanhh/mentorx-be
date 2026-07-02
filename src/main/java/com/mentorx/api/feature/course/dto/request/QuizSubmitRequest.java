package com.mentorx.api.feature.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmitRequest {
    @NotNull
    private UUID enrollmentId;

    @NotNull
    private UUID lessonId;

    @NotEmpty
    @Valid
    private List<QuizSubmittedAnswerRequest> answers;
}
