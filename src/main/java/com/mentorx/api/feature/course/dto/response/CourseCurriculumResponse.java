package com.mentorx.api.feature.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCurriculumResponse {
    private List<CourseSectionResponse> sections;
    private List<CourseLessonResponse> lessons;
    private List<QuizQuestionResponse> quizQuestions;
}
