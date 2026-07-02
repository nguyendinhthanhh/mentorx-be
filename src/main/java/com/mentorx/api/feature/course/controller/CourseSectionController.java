package com.mentorx.api.feature.course.controller;

import com.mentorx.api.feature.course.dto.request.CourseSectionCreateRequest;
import com.mentorx.api.feature.course.dto.request.CourseSectionUpdateRequest;
import com.mentorx.api.feature.course.dto.response.CourseSectionResponse;
import com.mentorx.api.feature.course.service.CourseSectionService;
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
@RequestMapping("/api/v1/course-sections")
@RequiredArgsConstructor
public class CourseSectionController {

    private final CourseSectionService sectionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseSectionResponse> createSection(@Valid @RequestBody CourseSectionCreateRequest request) {
        CourseSectionResponse response = sectionService.createSection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseSectionResponse> getSectionById(@PathVariable UUID id) {
        CourseSectionResponse response = sectionService.getSectionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseSectionResponse>> getSectionsByCourseId(@PathVariable UUID courseId) {
        List<CourseSectionResponse> responses = sectionService.getSectionsByCourseId(courseId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/course/{courseId}/paginated")
    public ResponseEntity<Page<CourseSectionResponse>> getSectionsByCourseIdPaginated(
            @PathVariable UUID courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sectionOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CourseSectionResponse> responses = sectionService.getSectionsByCourseId(courseId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/course/{courseId}/published")
    public ResponseEntity<List<CourseSectionResponse>> getPublishedSectionsByCourseId(@PathVariable UUID courseId) {
        List<CourseSectionResponse> responses = sectionService.getPublishedSectionsByCourseId(courseId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Long> countSectionsByCourseId(@PathVariable UUID courseId) {
        Long count = sectionService.countSectionsByCourseId(courseId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseSectionResponse> updateSection(
            @PathVariable UUID id,
            @Valid @RequestBody CourseSectionUpdateRequest request) {
        CourseSectionResponse response = sectionService.updateSection(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteSection(@PathVariable UUID id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }
}
