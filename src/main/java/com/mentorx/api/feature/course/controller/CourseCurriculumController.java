package com.mentorx.api.feature.course.controller;

import com.mentorx.api.feature.course.dto.request.CourseCurriculumSaveRequest;
import com.mentorx.api.feature.course.dto.response.CourseCurriculumResponse;
import com.mentorx.api.feature.course.service.CourseCurriculumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/course-curriculum")
@RequiredArgsConstructor
public class CourseCurriculumController {

    private final CourseCurriculumService curriculumService;

    @PutMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseCurriculumResponse> saveCurriculum(
            @PathVariable UUID courseId,
            @Valid @RequestBody CourseCurriculumSaveRequest request) {
        return ResponseEntity.ok(curriculumService.saveCurriculum(courseId, request));
    }
}
