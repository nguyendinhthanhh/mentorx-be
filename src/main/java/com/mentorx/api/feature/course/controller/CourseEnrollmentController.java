package com.mentorx.api.feature.course.controller;

import com.mentorx.api.feature.course.dto.request.CourseEnrollmentCreateRequest;
import com.mentorx.api.feature.course.dto.response.CourseEnrollmentResponse;
import com.mentorx.api.feature.course.dto.response.CourseStatsResponse;
import com.mentorx.api.feature.course.service.CertificateService;
import com.mentorx.api.feature.course.service.CourseEnrollmentService;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.course.repository.CourseEnrollmentRepository;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/course-enrollments")
@RequiredArgsConstructor
public class CourseEnrollmentController {

    private final CourseEnrollmentService enrollmentService;
    private final UserRepository userRepository;
    private final CertificateService certificateService;
    private final CourseEnrollmentRepository enrollmentRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'STUDENT', 'ADMIN')")
    public ResponseEntity<CourseEnrollmentResponse> createEnrollment(@Valid @RequestBody CourseEnrollmentCreateRequest request) {
        CourseEnrollmentResponse response = enrollmentService.createEnrollment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/course/{courseId}/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CourseEnrollmentResponse> enrollCurrentUser(
            @PathVariable UUID courseId,
            Authentication authentication) {
        User currentUser = resolveCurrentUser(authentication);
        CourseEnrollmentResponse response = enrollmentService.enrollCurrentUser(courseId, currentUser.getId());
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
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
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
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
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

    @GetMapping("/{enrollmentId}/certificate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable UUID enrollmentId) {
        byte[] content = certificateService.renderCertificate(enrollmentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mentorx-certificate.pdf\"")
                .body(content);
    }

    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Long> countEnrollmentsByCourseId(@PathVariable UUID courseId) {
        Long count = enrollmentService.countEnrollmentsByCourseId(courseId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/course/{courseId}/stats")
    public ResponseEntity<CourseStatsResponse> getCourseStats(@PathVariable UUID courseId) {
        long total = enrollmentRepository.countByCourseId(courseId);
        long completed = enrollmentRepository.countCompletedByCourseId(courseId);
        double completionRate = total == 0 ? 0 : (completed * 100.0) / total;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last7DaysStart = now.minusDays(7);
        LocalDateTime previous7DaysStart = now.minusDays(14);
        BigDecimal totalRevenue = enrollmentRepository.sumAmountPaidByCourseId(courseId);
        BigDecimal last7DaysRevenue = enrollmentRepository.sumAmountPaidByCourseIdAndEnrolledAtBetween(courseId, last7DaysStart, now);
        BigDecimal previous7DaysRevenue = enrollmentRepository.sumAmountPaidByCourseIdAndEnrolledAtBetween(courseId, previous7DaysStart, last7DaysStart);
        long last7DaysEnrollments = enrollmentRepository.countByCourseIdAndEnrolledAtBetween(courseId, last7DaysStart, now);
        long previous7DaysEnrollments = enrollmentRepository.countByCourseIdAndEnrolledAtBetween(courseId, previous7DaysStart, last7DaysStart);
        return ResponseEntity.ok(CourseStatsResponse.builder()
                .courseId(courseId)
                .totalEnrollments(total)
                .completedEnrollments(completed)
                .completionRate(completionRate)
                .totalRevenueMxc(totalRevenue)
                .last7DaysRevenueMxc(last7DaysRevenue)
                .last7DaysEnrollments(last7DaysEnrollments)
                .previous7DaysRevenueMxc(previous7DaysRevenue)
                .previous7DaysEnrollments(previous7DaysEnrollments)
                .build());
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

    @GetMapping("/course/{courseId}/me/is-enrolled")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> isCurrentUserEnrolled(@PathVariable UUID courseId, Authentication authentication) {
        User currentUser = resolveCurrentUser(authentication);
        boolean isEnrolled = enrollmentService.isStudentEnrolled(courseId, currentUser.getId());
        return ResponseEntity.ok(isEnrolled);
    }

    private User resolveCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
