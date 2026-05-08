package com.mentorx.api.feature.course.controller;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.course.dto.request.CourseCreateRequest;
import com.mentorx.api.feature.course.dto.request.CourseUpdateRequest;
import com.mentorx.api.feature.course.dto.response.CourseResponse;
import com.mentorx.api.feature.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> create(@Valid @RequestBody CourseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(courseService.create(request)));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> getById(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getById(courseId)));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> update(@PathVariable UUID courseId,
                                                              @RequestBody CourseUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(courseService.update(courseId, request)));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID courseId) {
        courseService.delete(courseId);
        return ResponseEntity.ok(ApiResponse.success("Course deleted", null));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getAllCourses(
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) UUID instructorId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                courseService.getAllCourses(status, instructorId, categoryId, PageRequest.of(page, size))
        ));
    }

    @GetMapping("/published")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getPublished(PageRequest.of(page, size))));
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getByInstructor(
            @PathVariable UUID instructorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getByInstructor(instructorId, PageRequest.of(page, size))));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getByStatus(
            @PathVariable CourseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getByStatus(status, PageRequest.of(page, size))));
    }

    @PatchMapping("/{courseId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateStatus(
            @PathVariable UUID courseId,
            @RequestParam CourseStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Course status updated", courseService.updateStatus(courseId, status)));
    }
}
