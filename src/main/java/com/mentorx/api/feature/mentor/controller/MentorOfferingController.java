package com.mentorx.api.feature.mentor.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.mentor.dto.request.MentorOfferingRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorOfferingResponse;
import com.mentorx.api.feature.mentor.service.MentorOfferingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
@Tag(name = "Mentor Offerings", description = "APIs for managing mentor offerings")
public class MentorOfferingController {

    private final MentorOfferingService mentorOfferingService;

    @PostMapping("/{userId}/courses")
    @Operation(summary = "Create mentor offering", description = "Create a new offering")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorOfferingResponse>> createCourse(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody MentorOfferingRequest request) {
        MentorOfferingResponse response = mentorOfferingService.createCourse(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Offering created successfully", response));
    }

    @PutMapping("/courses/{courseId}")
    @Operation(summary = "Update mentor offering", description = "Update an existing offering")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorOfferingResponse>> updateCourse(
            @Parameter(description = "Course ID") @PathVariable UUID courseId,
            @Valid @RequestBody MentorOfferingRequest request) {
        MentorOfferingResponse response = mentorOfferingService.updateCourse(courseId, request);
        return ResponseEntity.ok(ApiResponse.success("Offering updated successfully", response));
    }

    @DeleteMapping("/courses/{courseId}")
    @Operation(summary = "Delete mentor offering", description = "Delete an offering")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @Parameter(description = "Course ID") @PathVariable UUID courseId) {
        mentorOfferingService.deleteCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success("Offering deleted successfully", null));
    }

    @PatchMapping("/courses/{courseId}/publish")
    @Operation(summary = "Publish offering", description = "Publish an offering to make it visible")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorOfferingResponse>> publishCourse(
            @Parameter(description = "Course ID") @PathVariable UUID courseId) {
        MentorOfferingResponse response = mentorOfferingService.publishCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success("Offering published successfully", response));
    }

    @PatchMapping("/courses/{courseId}/archive")
    @Operation(summary = "Archive offering", description = "Archive an offering")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorOfferingResponse>> archiveCourse(
            @Parameter(description = "Course ID") @PathVariable UUID courseId) {
        MentorOfferingResponse response = mentorOfferingService.archiveCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success("Offering archived successfully", response));
    }

    @GetMapping("/courses/{courseId}")
    @Operation(summary = "Get offering by ID", description = "Get offering details by ID")
    public ResponseEntity<ApiResponse<MentorOfferingResponse>> getCourseById(
            @Parameter(description = "Course ID") @PathVariable UUID courseId) {
        MentorOfferingResponse response = mentorOfferingService.getCourseById(courseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}/courses")
    @Operation(summary = "Get all offerings for mentor", description = "Get all offerings for a specific mentor")
    public ResponseEntity<ApiResponse<List<MentorOfferingResponse>>> getAllCoursesByMentor(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<MentorOfferingResponse> response = mentorOfferingService.getAllCoursesByMentor(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}/courses/published")
    @Operation(summary = "Get published offerings for mentor", description = "Get all published offerings for a specific mentor")
    public ResponseEntity<ApiResponse<List<MentorOfferingResponse>>> getPublishedCoursesByMentor(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<MentorOfferingResponse> response = mentorOfferingService.getPublishedCoursesByMentor(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

