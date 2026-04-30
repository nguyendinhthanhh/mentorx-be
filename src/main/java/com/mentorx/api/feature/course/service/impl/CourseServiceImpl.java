package com.mentorx.api.feature.course.service.impl;

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

    @Override
    @Transactional
    public CourseResponse create(CourseCreateRequest request) {
        User instructor = userRepository.findById(request.instructorId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Course course = Course.builder()
                .instructor(instructor)
                .categoryId(request.categoryId())
                .title(request.title())
                .slug(request.slug())
                .description(request.description())
                .thumbnailUrl(request.thumbnailUrl())
                .priceMxc(request.priceMxc() != null ? request.priceMxc() : BigDecimal.ZERO)
                .language(request.language() != null ? request.language() : com.mentorx.api.common.enums.SupportedLanguage.vi)
                .level(request.level())
                .isCertificate(Boolean.TRUE.equals(request.isCertificate()))
                .previewVideoUrl(request.previewVideoUrl())
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
        if (request.categoryId() != null) course.setCategoryId(request.categoryId());
        if (request.title() != null) course.setTitle(request.title());
        if (request.description() != null) course.setDescription(request.description());
        if (request.thumbnailUrl() != null) course.setThumbnailUrl(request.thumbnailUrl());
        if (request.priceMxc() != null) course.setPriceMxc(request.priceMxc());
        if (request.language() != null) course.setLanguage(request.language());
        if (request.level() != null) course.setLevel(request.level());
        if (request.isCertificate() != null) course.setIsCertificate(request.isCertificate());
        if (request.previewVideoUrl() != null) course.setPreviewVideoUrl(request.previewVideoUrl());
        if (request.status() != null) {
            course.setStatus(request.status());
            if (request.status() == CourseStatus.PUBLISHED && course.getPublishedAt() == null) {
                course.setPublishedAt(LocalDateTime.now());
            }
        }
        return toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void delete(UUID courseId) {
        Course course = findCourse(courseId);
        course.setDeletedAt(LocalDateTime.now());
        courseRepository.save(course);
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

    private Course findCourse(UUID courseId) {
        return courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getInstructor().getId(),
                course.getInstructor().getFullName(),
                course.getCategoryId(),
                course.getTitle(),
                course.getSlug(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getPriceMxc(),
                course.getStatus(),
                course.getLanguage(),
                course.getLevel(),
                course.getTotalDurationMin(),
                course.getTotalLessons(),
                course.getTotalEnrollments(),
                course.getAverageRating(),
                course.getTotalReviews(),
                course.getIsCertificate(),
                course.getPreviewVideoUrl(),
                course.getRejectionReason(),
                course.getPublishedAt(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
