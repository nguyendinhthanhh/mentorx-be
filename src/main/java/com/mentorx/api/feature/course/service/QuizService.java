package com.mentorx.api.feature.course.service;

import com.mentorx.api.feature.course.dto.request.QuizQuestionRequest;
import com.mentorx.api.feature.course.dto.request.QuizSubmitRequest;
import com.mentorx.api.feature.course.dto.response.QuizAttemptResponse;
import com.mentorx.api.feature.course.dto.response.QuizQuestionResponse;

import java.util.List;
import java.util.UUID;

public interface QuizService {
    QuizQuestionResponse createQuestion(UUID lessonId, QuizQuestionRequest request);
    List<QuizQuestionResponse> getQuestions(UUID lessonId);
    QuizQuestionResponse updateQuestion(UUID questionId, QuizQuestionRequest request);
    void deleteQuestion(UUID questionId);
    QuizAttemptResponse submitAttempt(QuizSubmitRequest request);
}
