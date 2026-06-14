package com.mentorx.api.feature.course.controller;

import com.mentorx.api.feature.course.dto.request.QuizQuestionRequest;
import com.mentorx.api.feature.course.dto.request.QuizSubmitRequest;
import com.mentorx.api.feature.course.dto.response.QuizAttemptResponse;
import com.mentorx.api.feature.course.dto.response.QuizQuestionResponse;
import com.mentorx.api.feature.course.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/course-quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/lessons/{lessonId}/questions")
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<QuizQuestionResponse> createQuestion(
            @PathVariable UUID lessonId,
            @Valid @RequestBody QuizQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.createQuestion(lessonId, request));
    }

    @GetMapping("/lessons/{lessonId}/questions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuizQuestionResponse>> getQuestions(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(quizService.getQuestions(lessonId));
    }

    @PutMapping("/questions/{questionId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<QuizQuestionResponse> updateQuestion(
            @PathVariable UUID questionId,
            @Valid @RequestBody QuizQuestionRequest request) {
        return ResponseEntity.ok(quizService.updateQuestion(questionId, request));
    }

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID questionId) {
        quizService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/attempts/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizAttemptResponse> submitAttempt(@Valid @RequestBody QuizSubmitRequest request) {
        return ResponseEntity.ok(quizService.submitAttempt(request));
    }
}
