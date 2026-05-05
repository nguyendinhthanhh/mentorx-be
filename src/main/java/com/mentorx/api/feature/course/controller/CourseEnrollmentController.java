package com.mentorx.api.feature.course.controller;

import com.mentorx.api.feature.course.dto.request.CourseEnrollmentCreateRequest;
import com.mentorx.api.feature.course.dto.response.CourseEnrollmentResponse;
import com.mentorx.api.feature.course.service.CourseEnrollmentService;
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
@RequestMapping("/api/v1/course-enrollments")
@RequiredArgsConstructor
public class CourseEnrollmentController {

    private final CourseEnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<CourseEnrollmentResponse> createEnrollment(@Valid @RequestBody CourseEnrollmentCreateRequest request) {
        CourseEnrollmentResponse response = enrollmentService.createEnrollment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CourseEnrollmentResponse> getEnrollmentById(@PathVariable UUID id) {
        CourseEnrollmentResponse response = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CourseEnrollmentResponse> getEnrollmentByCourseAndStudent(
            @PathVariable UUID courseId,
            @PathVariable UUID studentId) {
        CourseEnrollmentResponse response = enrollmentService.getEnrollmentByCourseAndStudent(courseId, studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CourseEnrollmentResponse>> getEnrollmentsByStudentId(
            @PathVariable UUID studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "enrolledAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CourseEnrollmentResponse> responses = enrollmentService.getEnrollmentsByStudentId(studentId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Page<CourseEnrollmentResponse>> getEnrollmentsByCourseId(
            @PathVariable UUID courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "enrolledAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CourseEnrollmentResponse> responses = enrollmentService.getEnrollmentsByCourseId(courseId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/instructor/{instructorId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Page<CourseEnrollmentResponse>> getEnrollmentsByInstructorId(
            @PathVariable UUID instructorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "enrolledAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CourseEnrollmentResponse> responses = enrollmentService.getEnrollmentsByInstructorId(instructorId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/student/{studentId}/completed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CourseEnrollmentResponse>> getCompletedEnrollmentsByStudentId(@PathVariable UUID studentId) {
        List<CourseEnrollmentResponse> responses = enrollmentService.getCompletedEnrollmentsByStudentId(studentId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{enrollmentId}/update-progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateEnrollmentProgress(@PathVariable UUID enrollmentId) {
        enrollmentService.updateEnrollmentProgress(enrollmentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{enrollmentId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markEnrollmentAsCompleted(@PathVariable UUID enrollmentId) {
        enrollmentService.markEnrollmentAsCompleted(enrollmentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Long> countEnrollmentsByCourseId(@PathVariable UUID courseId) {
        Long count = enrollmentService.countEnrollmentsByCourseId(courseId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/student/{studentId}/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countEnrollmentsByStudentId(@PathVariable UUID studentId) {
        Long count = enrollmentService.countEnrollmentsByStudentId(studentId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/course/{courseId}/student/{studentId}/is-enrolled")
    public ResponseEntity<Boolean> isStudentEnrolled(
            @PathVariable UUID courseId,
            @PathVariable UUID studentId) {
        boolean isEnrolled = enrollmentService.isStudentEnrolled(courseId, studentId);
        return ResponseEntity.ok(isEnrolled);
    }
}
