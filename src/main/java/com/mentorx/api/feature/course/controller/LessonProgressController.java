package com.mentorx.api.feature.course.controller;

import com.mentorx.api.feature.course.dto.request.LessonProgressUpdateRequest;
import com.mentorx.api.feature.course.dto.response.LessonProgressResponse;
import com.mentorx.api.feature.course.service.LessonProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lesson-progress")
@RequiredArgsConstructor
public class LessonProgressController {

    private final LessonProgressService progressService;

    @PostMapping("/enrollment/{enrollmentId}/lesson/{lessonId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LessonProgressResponse> createOrUpdateProgress(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonProgressUpdateRequest request) {
        LessonProgressResponse response = progressService.createOrUpdateProgress(enrollmentId, lessonId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/enrollment/{enrollmentId}/lesson/{lessonId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LessonProgressResponse> getProgress(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID lessonId) {
        LessonProgressResponse response = progressService.getProgress(enrollmentId, lessonId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/enrollment/{enrollmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LessonProgressResponse>> getProgressByEnrollmentId(@PathVariable UUID enrollmentId) {
        List<LessonProgressResponse> responses = progressService.getProgressByEnrollmentId(enrollmentId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LessonProgressResponse>> getProgressByStudentAndCourse(
            @PathVariable UUID studentId,
            @PathVariable UUID courseId) {
        List<LessonProgressResponse> responses = progressService.getProgressByStudentAndCourse(studentId, courseId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/enrollment/{enrollmentId}/lesson/{lessonId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markLessonAsCompleted(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID lessonId) {
        progressService.markLessonAsCompleted(enrollmentId, lessonId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/enrollment/{enrollmentId}/completed-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countCompletedLessons(@PathVariable UUID enrollmentId) {
        Long count = progressService.countCompletedLessons(enrollmentId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/enrollment/{enrollmentId}/total-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countTotalLessons(@PathVariable UUID enrollmentId) {
        Long count = progressService.countTotalLessons(enrollmentId);
        return ResponseEntity.ok(count);
    }
}
