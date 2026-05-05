package com.mentorx.api.feature.course.service;

import com.mentorx.api.feature.course.dto.request.CourseEnrollmentCreateRequest;
import com.mentorx.api.feature.course.dto.response.CourseEnrollmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CourseEnrollmentService {
    
    CourseEnrollmentResponse createEnrollment(CourseEnrollmentCreateRequest request);
    
    CourseEnrollmentResponse getEnrollmentById(UUID id);
    
    CourseEnrollmentResponse getEnrollmentByCourseAndStudent(UUID courseId, UUID studentId);
    
    Page<CourseEnrollmentResponse> getEnrollmentsByStudentId(UUID studentId, Pageable pageable);
    
    Page<CourseEnrollmentResponse> getEnrollmentsByCourseId(UUID courseId, Pageable pageable);
    
    Page<CourseEnrollmentResponse> getEnrollmentsByInstructorId(UUID instructorId, Pageable pageable);
    
    List<CourseEnrollmentResponse> getCompletedEnrollmentsByStudentId(UUID studentId);
    
    void updateEnrollmentProgress(UUID enrollmentId);
    
    void markEnrollmentAsCompleted(UUID enrollmentId);
    
    Long countEnrollmentsByCourseId(UUID courseId);
    
    Long countEnrollmentsByStudentId(UUID studentId);
    
    boolean isStudentEnrolled(UUID courseId, UUID studentId);
}
