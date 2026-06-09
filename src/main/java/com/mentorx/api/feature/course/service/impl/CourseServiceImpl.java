package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.common.util.CloudinaryMediaService;
import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.enums.SupportedLanguage;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.course.dto.request.CourseCreateRequest;
import com.mentorx.api.feature.course.dto.request.CourseUpdateRequest;
import com.mentorx.api.feature.course.dto.response.CourseResponse;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.system.entity.Skill;
import com.mentorx.api.feature.system.repository.SkillRepository;
import com.mentorx.api.feature.course.service.CourseService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final MentorModeAccessService mentorModeAccessService;
    private final SkillRepository skillRepository;
    private final CloudinaryMediaService cloudinaryMediaService;

    @Override
    @Transactional
    public CourseResponse create(CourseCreateRequest request) {
        mentorModeAccessService.requireApprovedMentorContentAccess(request.getInstructorId());
        User instructor = userRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        requireAtLeastOneSkill(request.getSkillIds(), request.getSkills());

        Course course = Course.builder()
                .instructor(instructor)
                .categoryId(request.getCategoryId())
                .skillIds(normalizeSkillIds(request.getSkillIds()))
                .skills(resolveSkillLabels(request.getSkillIds(), request.getSkills()))
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
        applyCourseUpdate(course, request);
        return toResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateDetailsWithMedia(UUID courseId,
                                                 CourseUpdateRequest request,
                                                 MultipartFile thumbnailFile,
                                                 MultipartFile previewVideoFile,
                                                 boolean removeThumbnail,
                                                 boolean removePreviewVideo) {
        Course course = findCourse(courseId);
        mentorModeAccessService.requireApprovedMentorContentAccess(course.getInstructor().getId());

        String previousThumbnailUrl = course.getThumbnailUrl();
        String previousPreviewVideoUrl = course.getPreviewVideoUrl();

        if (hasFile(thumbnailFile)) {
            validateCourseMedia(thumbnailFile, true);
            request.setThumbnailUrl(cloudinaryMediaService
                    .uploadCourseMedia(thumbnailFile, "mentorx/courses/previews/images")
                    .getFileUrl());
        } else if (removeThumbnail) {
            request.setThumbnailUrl("");
        }

        if (hasFile(previewVideoFile)) {
            validateCourseMedia(previewVideoFile, false);
            request.setPreviewVideoUrl(cloudinaryMediaService
                    .uploadCourseMedia(previewVideoFile, "mentorx/courses/previews/videos")
                    .getFileUrl());
        } else if (removePreviewVideo) {
            request.setPreviewVideoUrl("");
        }

        applyCourseUpdate(course, request);
        deleteReplacedCourseMedia(previousThumbnailUrl, course.getThumbnailUrl());
        deleteReplacedCourseMedia(previousPreviewVideoUrl, course.getPreviewVideoUrl());
        return toResponse(course);
    }

    private void applyCourseUpdate(Course course, CourseUpdateRequest request) {
        if (request.getCategoryId() != null) course.setCategoryId(request.getCategoryId());
        if (request.getSkillIds() != null) {
            course.setSkillIds(normalizeSkillIds(request.getSkillIds()));
            course.setSkills(resolveSkillLabels(request.getSkillIds(), request.getSkills()));
        } else if (request.getSkills() != null) {
            course.setSkills(normalizeSkills(request.getSkills()));
        }
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
    }

    @Override
    @Transactional
    public void delete(UUID courseId) {
        Course course = findCourse(courseId);
        mentorModeAccessService.requireApprovedMentorContentAccess(course.getInstructor().getId());
        if (course.getStatus() != CourseStatus.DRAFT && course.getStatus() != CourseStatus.REJECTED) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Only draft or rejected courses can be deleted");
        }
        course.setDeletedAt(LocalDateTime.now());
    }

    @Override
    @Transactional
    public CourseResponse archive(UUID courseId) {
        Course course = findCourse(courseId);
        mentorModeAccessService.requireApprovedMentorContentAccess(course.getInstructor().getId());
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Only published courses can be archived");
        }
        course.setStatus(CourseStatus.ARCHIVED);
        return toResponse(course);
    }

    @Override
    public Page<CourseResponse> getAllCourses(CourseStatus status,
                                              UUID instructorId,
                                              Integer categoryId,
                                              SupportedLanguage language,
                                              String levelKeyword,
                                              String skillKeyword,
                                              Pageable pageable) {
        String normalizedLevelKeyword = normalizeKeyword(levelKeyword);
        String normalizedSkillKeyword = normalizeKeyword(skillKeyword);
        return courseRepository
                .findAllWithFilters(enumName(status), instructorId, categoryId, enumName(language), normalizedLevelKeyword, normalizedSkillKeyword, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<CourseResponse> getPublished(Integer categoryId,
                                             SupportedLanguage language,
                                             String levelKeyword,
                                             String skillKeyword,
                                             Pageable pageable) {
        String normalizedLevelKeyword = normalizeKeyword(levelKeyword);
        String normalizedSkillKeyword = normalizeKeyword(skillKeyword);
        return courseRepository
                .findPublishedWithFilters(CourseStatus.PUBLISHED.name(), categoryId, enumName(language), normalizedLevelKeyword, normalizedSkillKeyword, pageable)
                .map(this::toResponse);
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
    public CourseResponse submitForReview(UUID courseId) {
        Course course = findCourse(courseId);
        mentorModeAccessService.requireApprovedMentorContentAccess(course.getInstructor().getId());
        if (course.getStatus() != CourseStatus.DRAFT && course.getStatus() != CourseStatus.REJECTED) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Only draft or rejected courses can be submitted for review");
        }
        course.setStatus(CourseStatus.PENDING_REVIEW);
        course.setSubmittedAt(LocalDateTime.now());
        course.setRejectionReason(null);
        return toResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateStatus(UUID courseId, CourseStatus status, String reason) {
        Course course = findCourse(courseId);
        User reviewer = userRepository.findById(mentorModeAccessService.getCurrentUserId()).orElse(null);
        course.setStatus(status);
        if (status == CourseStatus.DRAFT && reason != null && !reason.trim().isBlank()) {
            course.setRejectionReason(reason.trim());
            course.setSubmittedAt(null);
        } else if (status == CourseStatus.REJECTED) {
            course.setRejectionReason(reason);
        } else if (status == CourseStatus.PENDING_REVIEW) {
            course.setRejectionReason(null);
        } else if (status == CourseStatus.PUBLISHED) {
            course.setRejectionReason(null);
        }
        course.setReviewedBy(reviewer);
        course.setReviewedAt(LocalDateTime.now());
        if (status == CourseStatus.PUBLISHED && course.getPublishedAt() == null) {
            course.setPublishedAt(LocalDateTime.now());
        }
        return toResponse(course);
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private void validateCourseMedia(MultipartFile file, boolean image) {
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        if (image) {
            if (!contentType.startsWith("image/")) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Course thumbnail must be an image file");
            }
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Course thumbnail must be 5 MB or smaller");
            }
            return;
        }
        if (!contentType.startsWith("video/")) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Preview video must be a video file");
        }
        if (file.getSize() > 200 * 1024 * 1024) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Preview video must be 200 MB or smaller");
        }
    }

    private void deleteReplacedCourseMedia(String previousUrl, String nextUrl) {
        if (previousUrl == null || previousUrl.isBlank()) {
            return;
        }
        if (previousUrl.equals(nextUrl)) {
            return;
        }
        cloudinaryMediaService.deleteCourseMedia(previousUrl);
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
                .skillIds(course.getSkillIds())
                .skills(resolveSkillLabels(course.getSkillIds(), course.getSkills()))
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
                .submittedAt(course.getSubmittedAt())
                .publishedAt(course.getPublishedAt())
                .reviewedBy(course.getReviewedBy() != null ? course.getReviewedBy().getId() : null)
                .reviewedAt(course.getReviewedAt())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .deletedAt(course.getDeletedAt())
                .build();
    }

    private List<String> normalizeSkills(List<String> skills) {
        if (skills == null) {
            return new ArrayList<>();
        }
        LinkedHashSet<String> deduped = new LinkedHashSet<>();
        for (String skill : skills) {
            if (skill == null) continue;
            String normalized = skill.trim();
            if (!normalized.isBlank()) {
                deduped.add(normalized);
            }
        }
        return new ArrayList<>(deduped);
    }

    private List<Integer> normalizeSkillIds(List<Integer> skillIds) {
        if (skillIds == null) {
            return new ArrayList<>();
        }
        LinkedHashSet<Integer> deduped = new LinkedHashSet<>();
        for (Integer skillId : skillIds) {
            if (skillId != null) {
                deduped.add(skillId);
            }
        }
        if (deduped.isEmpty()) {
            return new ArrayList<>();
        }
        List<Skill> activeSkills = skillRepository.findAllById(deduped).stream()
                .filter(skill -> Boolean.TRUE.equals(skill.getIsActive()))
                .toList();
        if (activeSkills.size() != deduped.size()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "One or more selected skills are invalid or inactive");
        }
        return new ArrayList<>(deduped);
    }

    private void requireAtLeastOneSkill(List<Integer> skillIds, List<String> skills) {
        boolean hasSkillId = skillIds != null && skillIds.stream().anyMatch(java.util.Objects::nonNull);
        boolean hasLegacySkill = skills != null && skills.stream().anyMatch(skill -> skill != null && !skill.trim().isBlank());
        if (!hasSkillId && !hasLegacySkill) {
            throw new AppException(ErrorCode.BAD_REQUEST, "At least one skill is required");
        }
    }

    private List<String> resolveSkillLabels(List<Integer> skillIds, List<String> fallbackSkills) {
        List<Integer> normalizedIds = normalizeSkillIdsForRead(skillIds);
        if (normalizedIds.isEmpty()) {
            return normalizeSkills(fallbackSkills);
        }
        return new ArrayList<>(skillRepository.findAllById(normalizedIds).stream()
                .sorted(java.util.Comparator.comparingInt(skill -> normalizedIds.indexOf(skill.getId())))
                .map(Skill::getLabelEn)
                .toList());
    }

    private List<Integer> normalizeSkillIdsForRead(List<Integer> skillIds) {
        if (skillIds == null) {
            return new ArrayList<>();
        }
        LinkedHashSet<Integer> deduped = new LinkedHashSet<>();
        for (Integer skillId : skillIds) {
            if (skillId != null) {
                deduped.add(skillId);
            }
        }
        return new ArrayList<>(deduped);
    }

    private String normalizeKeyword(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
