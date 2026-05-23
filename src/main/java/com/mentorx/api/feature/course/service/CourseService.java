package com.mentorx.api.feature.course.service;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.enums.SupportedLanguage;
import com.mentorx.api.feature.course.dto.request.CourseCreateRequest;
import com.mentorx.api.feature.course.dto.request.CourseUpdateRequest;
import com.mentorx.api.feature.course.dto.response.CourseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CourseService {
    CourseResponse create(CourseCreateRequest request);
    CourseResponse getById(UUID courseId);
    CourseResponse update(UUID courseId, CourseUpdateRequest request);
    void delete(UUID courseId);
    Page<CourseResponse> getAllCourses(CourseStatus status,
                                       UUID instructorId,
                                       Integer categoryId,
                                       SupportedLanguage language,
                                       String levelKeyword,
                                       String skillKeyword,
                                       Pageable pageable);
    Page<CourseResponse> getPublished(Integer categoryId,
                                      SupportedLanguage language,
                                      String levelKeyword,
                                      String skillKeyword,
                                      Pageable pageable);
    Page<CourseResponse> getByInstructor(UUID instructorId, Pageable pageable);
    Page<CourseResponse> getByStatus(CourseStatus status, Pageable pageable);
    CourseResponse updateStatus(UUID courseId, CourseStatus status, String reason);
}
