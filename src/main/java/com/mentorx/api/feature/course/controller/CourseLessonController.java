package com.mentorx.api.feature.course.controller;

import com.mentorx.api.feature.course.dto.request.CourseLessonCreateRequest;
import com.mentorx.api.feature.course.dto.request.CourseLessonUpdateRequest;
import com.mentorx.api.feature.course.dto.response.CourseLessonResponse;
import com.mentorx.api.feature.course.service.CourseLessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/course-lessons")
@RequiredArgsConstructor
public class CourseLessonController {

    private final CourseLessonService lessonService;

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseLessonResponse> createLesson(@Valid @RequestBody CourseLessonCreateRequest request) {
        CourseLessonResponse response = lessonService.createLesson(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseLessonResponse> getLessonById(@PathVariable UUID id) {
        CourseLessonResponse response = lessonService.getLessonById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<CourseLessonResponse>> getLessonsBySectionId(@PathVariable UUID sectionId) {
        List<CourseLessonResponse> responses = lessonService.getLessonsBySectionId(sectionId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/section/{sectionId}/paginated")
    public ResponseEntity<Page<CourseLessonResponse>> getLessonsBySectionIdPaginated(
            @PathVariable UUID sectionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lessonOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CourseLessonResponse> responses = lessonService.getLessonsBySectionId(sectionId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseLessonResponse>> getAllLessonsByCourseId(@PathVariable UUID courseId) {
        List<CourseLessonResponse> responses = lessonService.getAllLessonsByCourseId(courseId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/course/{courseId}/free-preview")
    public ResponseEntity<List<CourseLessonResponse>> getFreePreviewLessonsByCourseId(@PathVariable UUID courseId) {
        List<CourseLessonResponse> responses = lessonService.getFreePreviewLessonsByCourseId(courseId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/section/{sectionId}/count")
    public ResponseEntity<Long> countLessonsBySectionId(@PathVariable UUID sectionId) {
        Long count = lessonService.countLessonsBySectionId(sectionId);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/{id}/view")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> incrementViewCount(@PathVariable UUID id) {
        lessonService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseLessonResponse> updateLesson(
            @PathVariable UUID id,
            @Valid @RequestBody CourseLessonUpdateRequest request) {
        CourseLessonResponse response = lessonService.updateLesson(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
}
