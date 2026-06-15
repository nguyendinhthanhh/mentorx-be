package com.mentorx.api.feature.course.controller;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.enums.CourseProductType;
import com.mentorx.api.common.enums.SupportedLanguage;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping(value = "/{courseId}/details", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateDetailsWithMedia(
            @PathVariable UUID courseId,
            @RequestPart("data") CourseUpdateRequest request,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestPart(value = "previewVideoFile", required = false) MultipartFile previewVideoFile,
            @RequestParam(defaultValue = "false") boolean removeThumbnail,
            @RequestParam(defaultValue = "false") boolean removePreviewVideo) {
        return ResponseEntity.ok(ApiResponse.success(
                courseService.updateDetailsWithMedia(courseId, request, thumbnailFile, previewVideoFile, removeThumbnail, removePreviewVideo)
        ));
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID courseId) {
        courseService.delete(courseId);
        return ResponseEntity.ok(ApiResponse.success("Course deleted", null));
    }

    @PostMapping("/{courseId}/archive")
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> archive(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success("Course archived", courseService.archive(courseId)));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getAllCourses(
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) CourseProductType productType,
            @RequestParam(required = false) UUID instructorId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) SupportedLanguage language,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                courseService.getAllCourses(status, productType, instructorId, categoryId, language, level, skill, PageRequest.of(page, size))
        ));
    }

    @GetMapping("/published")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getPublished(
            @RequestParam(required = false) CourseProductType productType,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) SupportedLanguage language,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                courseService.getPublished(productType, categoryId, language, level, skill, PageRequest.of(page, size))
        ));
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

    @PostMapping("/{courseId}/submit-for-review")
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> submitForReview(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success("Course submitted for review", courseService.submitForReview(courseId)));
    }

    @PatchMapping("/{courseId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateStatus(
            @PathVariable UUID courseId,
            @RequestParam CourseStatus status,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("Course status updated", courseService.updateStatus(courseId, status, reason)));
    }
}
