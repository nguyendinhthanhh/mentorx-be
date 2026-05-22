package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.course.dto.request.CourseCreateRequest;
import com.mentorx.api.feature.course.dto.request.CourseUpdateRequest;
import com.mentorx.api.feature.course.dto.response.CourseResponse;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.course.service.CourseService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final MentorModeAccessService mentorModeAccessService;

    @Override
    @Transactional
    public CourseResponse create(CourseCreateRequest request) {
        mentorModeAccessService.requireApprovedMentorContentAccess(request.getInstructorId());
        User instructor = userRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Course course = Course.builder()
                .instructor(instructor)
                .categoryId(request.getCategoryId())
                .title(request.getTitle())
                .slug(request.getSlug())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .priceMxc(request.getPriceMxc() != null ? request.getPriceMxc() : BigDecimal.ZERO)
                .language(request.getLanguage() != null ? request.getLanguage() : com.mentorx.api.common.enums.SupportedLanguage.vi)
                .level(request.getLevel())
                .isCertificate(Boolean.TRUE.equals(request.getIsCertificate()))
                .previewVideoUrl(request.getPreviewVideoUrl())
                .status(CourseStatus.DRAFT)
                .build();
        return toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse getById(UUID courseId) {
        return toResponse(findCourse(courseId));
    }

    @Override
    @Transactional
    public CourseResponse update(UUID courseId, CourseUpdateRequest request) {
        Course course = findCourse(courseId);
        mentorModeAccessService.requireApprovedMentorContentAccess(course.getInstructor().getId());
        if (request.getCategoryId() != null) course.setCategoryId(request.getCategoryId());
        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getThumbnailUrl() != null) course.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getPriceMxc() != null) course.setPriceMxc(request.getPriceMxc());
        if (request.getLanguage() != null) course.setLanguage(request.getLanguage());
        if (request.getLevel() != null) course.setLevel(request.getLevel());
        if (request.getIsCertificate() != null) course.setIsCertificate(request.getIsCertificate());
        if (request.getPreviewVideoUrl() != null) course.setPreviewVideoUrl(request.getPreviewVideoUrl());
        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
            if (request.getStatus() == CourseStatus.PUBLISHED && course.getPublishedAt() == null) {
                course.setPublishedAt(LocalDateTime.now());
            }
        }
        return toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void delete(UUID courseId) {
        Course course = findCourse(courseId);
        mentorModeAccessService.requireApprovedMentorContentAccess(course.getInstructor().getId());
        course.setDeletedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    @Override
    public Page<CourseResponse> getAllCourses(CourseStatus status, UUID instructorId, Integer categoryId, Pageable pageable) {
        return courseRepository.findAllWithFilters(status, instructorId, categoryId, pageable).map(this::toResponse);
    }

    @Override
    public Page<CourseResponse> getPublished(Pageable pageable) {
        return courseRepository.findByStatusAndDeletedAtIsNull(CourseStatus.PUBLISHED, pageable).map(this::toResponse);
    }

    @Override
    public Page<CourseResponse> getByInstructor(UUID instructorId, Pageable pageable) {
        return courseRepository.findByInstructorIdAndDeletedAtIsNull(instructorId, pageable).map(this::toResponse);
    }

    @Override
    public Page<CourseResponse> getByStatus(CourseStatus status, Pageable pageable) {
        return courseRepository.findByStatusAndDeletedAtIsNull(status, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public CourseResponse updateStatus(UUID courseId, CourseStatus status, String reason) {
        Course course = findCourse(courseId);
        course.setStatus(status);
        if (reason != null) {
            course.setRejectionReason(reason);
        }
        if (status == CourseStatus.PUBLISHED && course.getPublishedAt() == null) {
            course.setPublishedAt(LocalDateTime.now());
        }
        return toResponse(courseRepository.save(course));
    }

    private Course findCourse(UUID courseId) {
        return courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
    }

    private CourseResponse toResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .instructorId(course.getInstructor().getId())
                .instructorName(course.getInstructor().getFullName())
                .categoryId(course.getCategoryId())
                .title(course.getTitle())
                .slug(course.getSlug())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .priceMxc(course.getPriceMxc())
                .status(course.getStatus())
                .language(course.getLanguage())
                .level(course.getLevel())
                .totalDurationMin(course.getTotalDurationMin())
                .totalLessons(course.getTotalLessons())
                .totalEnrollments(course.getTotalEnrollments())
                .averageRating(course.getAverageRating())
                .totalReviews(course.getTotalReviews())
                .isCertificate(course.getIsCertificate())
                .previewVideoUrl(course.getPreviewVideoUrl())
                .rejectionReason(course.getRejectionReason())
                .publishedAt(course.getPublishedAt())
                .reviewedBy(course.getReviewedBy() != null ? course.getReviewedBy().getId() : null)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .deletedAt(course.getDeletedAt())
                .build();
    }
}
