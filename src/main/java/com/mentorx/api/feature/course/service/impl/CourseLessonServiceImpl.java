package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.exception.ErrorCode;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.feature.course.dto.request.CourseLessonCreateRequest;
import com.mentorx.api.feature.course.dto.request.CourseLessonUpdateRequest;
import com.mentorx.api.feature.course.dto.response.CourseLessonResponse;
import com.mentorx.api.feature.course.entity.CourseLesson;
import com.mentorx.api.feature.course.entity.CourseSection;
import com.mentorx.api.feature.course.mapper.CourseMapper;
import com.mentorx.api.feature.course.repository.CourseLessonRepository;
import com.mentorx.api.feature.course.repository.CourseSectionRepository;
import com.mentorx.api.feature.course.service.CourseLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseLessonServiceImpl implements CourseLessonService {

    private final CourseLessonRepository lessonRepository;
    private final CourseSectionRepository sectionRepository;
    private final CourseMapper mapper;

    @Override
    @Transactional
    public CourseLessonResponse createLesson(CourseLessonCreateRequest request) {
        log.info("Creating new course lesson for section: {}", request.getSectionId());

        CourseSection section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_SECTION_NOT_FOUND));

        if (lessonRepository.existsBySectionIdAndLessonOrder(request.getSectionId(), request.getLessonOrder())) {
            throw new AppException(ErrorCode.LESSON_ORDER_EXISTS);
        }
        validateLessonContent(request.getLessonType(), request.getVideoUrl(), request.getArticleContent(), request.getResourceUrl());

        CourseLesson lesson = mapper.toEntity(request);
        lesson.setSection(section);
        
        if (lesson.getIsFreePreview() == null) {
            lesson.setIsFreePreview(false);
        }
        if (lesson.getIsPublished() == null) {
            lesson.setIsPublished(true);
        }
        if (lesson.getIsMandatory() == null) {
            lesson.setIsMandatory(true);
        }

        CourseLesson savedLesson = lessonRepository.save(lesson);
        log.info("Course lesson created successfully with ID: {}", savedLesson.getId());

        return mapper.toResponse(savedLesson);
    }

    @Override
    public CourseLessonResponse getLessonById(UUID id) {
        log.debug("Fetching course lesson by ID: {}", id);
        
        CourseLesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        return mapper.toResponse(lesson);
    }

    @Override
    public List<CourseLessonResponse> getLessonsBySectionId(UUID sectionId) {
        log.debug("Fetching all lessons for section: {}", sectionId);
        
        List<CourseLesson> lessons = lessonRepository.findBySectionIdOrderByLessonOrderAsc(sectionId);
        return mapper.toLessonResponseList(lessons);
    }

    @Override
    public Page<CourseLessonResponse> getLessonsBySectionId(UUID sectionId, Pageable pageable) {
        log.debug("Fetching lessons for section: {} with pagination", sectionId);
        
        Page<CourseLesson> lessons = lessonRepository.findBySectionId(sectionId, pageable);
        return lessons.map(mapper::toResponse);
    }

    @Override
    public List<CourseLessonResponse> getAllLessonsByCourseId(UUID courseId) {
        log.debug("Fetching all lessons for course: {}", courseId);
        
        List<CourseLesson> lessons = lessonRepository.findAllByCourseId(courseId);
        return mapper.toLessonResponseList(lessons);
    }

    @Override
    @Transactional
    public CourseLessonResponse updateLesson(UUID id, CourseLessonUpdateRequest request) {
        log.info("Updating course lesson: {}", id);

        CourseLesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (request.getLessonOrder() != null && 
            !request.getLessonOrder().equals(lesson.getLessonOrder()) &&
            lessonRepository.existsBySectionIdAndLessonOrder(lesson.getSection().getId(), request.getLessonOrder())) {
            throw new AppException(ErrorCode.LESSON_ORDER_EXISTS);
        }
        validateLessonContent(
                request.getLessonType() == null ? lesson.getLessonType() : request.getLessonType(),
                request.getVideoUrl() == null ? lesson.getVideoUrl() : request.getVideoUrl(),
                request.getArticleContent() == null ? lesson.getArticleContent() : request.getArticleContent(),
                request.getResourceUrl() == null ? lesson.getResourceUrl() : request.getResourceUrl()
        );

        mapper.updateEntity(request, lesson);
        CourseLesson updatedLesson = lessonRepository.save(lesson);
        
        log.info("Course lesson updated successfully: {}", id);
        return mapper.toResponse(updatedLesson);
    }

    @Override
    @Transactional
    public void deleteLesson(UUID id) {
        log.info("Deleting course lesson: {}", id);

        if (!lessonRepository.existsById(id)) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND);
        }

        lessonRepository.deleteById(id);
        log.info("Course lesson deleted successfully: {}", id);
    }

    @Override
    public Long countLessonsBySectionId(UUID sectionId) {
        log.debug("Counting lessons for section: {}", sectionId);
        return lessonRepository.countBySectionId(sectionId);
    }

    @Override
    public List<CourseLessonResponse> getFreePreviewLessonsByCourseId(UUID courseId) {
        log.debug("Fetching free preview lessons for course: {}", courseId);
        
        List<CourseLesson> lessons = lessonRepository.findFreePreviewLessonsByCourseId(courseId);
        return mapper.toLessonResponseList(lessons);
    }

    @Override
    @Transactional
    public void incrementViewCount(UUID lessonId) {
        log.debug("Incrementing view count for lesson: {}", lessonId);
        
        CourseLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        lesson.incrementViewCount();
        lessonRepository.save(lesson);
    }

    private void validateLessonContent(com.mentorx.api.common.enums.LessonType lessonType,
                                       String videoUrl,
                                       String articleContent,
                                       String resourceUrl) {
        if (lessonType == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Lesson type is required");
        }
        switch (lessonType) {
            case VIDEO, ARTICLE -> {
                // Video and rich text content can be attached later while the lesson remains a draft.
            }
            case DOWNLOADABLE -> {
                if (resourceUrl == null || resourceUrl.isBlank()) {
                    throw new AppException(ErrorCode.BAD_REQUEST, "Downloadable lessons require a resource URL");
                }
            }
            case QUIZ, ASSIGNMENT, LIVE_SESSION -> {
                // These lesson types can be created before their details are attached.
            }
        }
    }
}
