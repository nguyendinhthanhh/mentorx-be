package com.mentorx.api.feature.course.service;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.enums.CourseProductType;
import com.mentorx.api.common.enums.SupportedLanguage;
import com.mentorx.api.feature.course.dto.request.CourseCreateRequest;
import com.mentorx.api.feature.course.dto.request.CourseUpdateRequest;
import com.mentorx.api.feature.course.dto.response.CourseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface CourseService {
    CourseResponse create(CourseCreateRequest request);
    CourseResponse getById(UUID courseId);
    CourseResponse update(UUID courseId, CourseUpdateRequest request);
    CourseResponse updateDetailsWithMedia(UUID courseId,
                                          CourseUpdateRequest request,
                                          MultipartFile thumbnailFile,
                                          MultipartFile previewVideoFile,
                                          boolean removeThumbnail,
                                          boolean removePreviewVideo);
    void delete(UUID courseId);
    CourseResponse archive(UUID courseId);
    Page<CourseResponse> getAllCourses(CourseStatus status,
                                       CourseProductType productType,
                                       UUID instructorId,
                                       Integer categoryId,
                                       SupportedLanguage language,
                                       String levelKeyword,
                                       String skillKeyword,
                                       Pageable pageable);
    Page<CourseResponse> getPublished(CourseProductType productType,
                                      Integer categoryId,
                                      SupportedLanguage language,
                                      String levelKeyword,
                                      String skillKeyword,
                                      Pageable pageable);
    Page<CourseResponse> getByInstructor(UUID instructorId, Pageable pageable);
    Page<CourseResponse> getByStatus(CourseStatus status, Pageable pageable);
    CourseResponse submitForReview(UUID courseId);
    CourseResponse updateStatus(UUID courseId, CourseStatus status, String reason);
}
