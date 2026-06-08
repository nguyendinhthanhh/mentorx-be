package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.enums.LessonType;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.feature.course.dto.request.CourseCurriculumSaveRequest;
import com.mentorx.api.feature.course.dto.response.CourseCurriculumResponse;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.entity.CourseLesson;
import com.mentorx.api.feature.course.entity.CourseSection;
import com.mentorx.api.feature.course.mapper.CourseMapper;
import com.mentorx.api.feature.course.repository.CourseLessonRepository;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.course.repository.CourseSectionRepository;
import com.mentorx.api.feature.course.service.CourseCurriculumService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseCurriculumServiceImpl implements CourseCurriculumService {

    private final CourseRepository courseRepository;
    private final CourseSectionRepository sectionRepository;
    private final CourseLessonRepository lessonRepository;
    private final CourseMapper mapper;
    private final MentorModeAccessService mentorModeAccessService;

    @Override
    @Transactional
    public CourseCurriculumResponse saveCurriculum(UUID courseId, CourseCurriculumSaveRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        mentorModeAccessService.requireApprovedMentorContentAccess(course.getInstructor().getId());

        List<CourseSection> existingSections = sectionRepository.findByCourseIdOrderBySectionOrderAsc(courseId);
        Map<UUID, CourseSection> existingSectionById = existingSections.stream()
                .collect(Collectors.toMap(CourseSection::getId, section -> section));
        List<CourseLesson> existingLessons = lessonRepository.findAllByCourseId(courseId);
        Map<UUID, CourseLesson> existingLessonById = existingLessons.stream()
                .collect(Collectors.toMap(CourseLesson::getId, lesson -> lesson));

        Set<UUID> retainedSectionIds = new HashSet<>();
        Set<UUID> retainedLessonIds = new HashSet<>();
        List<CourseSection> savedSections = new ArrayList<>();

        for (CourseCurriculumSaveRequest.SectionItem sectionItem : request.getSections()) {
            CourseSection section = sectionItem.getId() == null
                    ? new CourseSection()
                    : existingSectionById.get(sectionItem.getId());
            if (section == null) {
                throw new AppException(ErrorCode.COURSE_SECTION_NOT_FOUND);
            }
            section.setCourse(course);
            section.setTitle(sectionItem.getTitle().trim());
            section.setDescription(blankToNull(sectionItem.getDescription()));
            section.setSectionOrder(sectionItem.getSectionOrder());
            section.setIsPublished(sectionItem.getIsPublished() == null || sectionItem.getIsPublished());
            CourseSection savedSection = sectionRepository.save(section);
            retainedSectionIds.add(savedSection.getId());
            savedSections.add(savedSection);

            List<CourseCurriculumSaveRequest.LessonItem> lessons = sectionItem.getLessons() == null
                    ? List.of()
                    : sectionItem.getLessons();
            for (CourseCurriculumSaveRequest.LessonItem lessonItem : lessons) {
                validateLessonContent(lessonItem);
                CourseLesson lesson = lessonItem.getId() == null
                        ? new CourseLesson()
                        : existingLessonById.get(lessonItem.getId());
                if (lesson == null) {
                    throw new AppException(ErrorCode.LESSON_NOT_FOUND);
                }
                lesson.setSection(savedSection);
                lesson.setTitle(lessonItem.getTitle().trim());
                lesson.setDescription(blankToNull(lessonItem.getDescription()));
                lesson.setLessonType(lessonItem.getLessonType());
                lesson.setLessonOrder(lessonItem.getLessonOrder());
                lesson.setDurationMinutes(lessonItem.getDurationMinutes());
                lesson.setVideoUrl(blankToNull(lessonItem.getVideoUrl()));
                lesson.setArticleContent(blankToNull(lessonItem.getArticleContent()));
                lesson.setResourceUrl(blankToNull(lessonItem.getResourceUrl()));
                lesson.setIsFreePreview(Boolean.TRUE.equals(lessonItem.getIsFreePreview()));
                lesson.setIsPublished(lessonItem.getIsPublished() == null || lessonItem.getIsPublished());
                lesson.setIsMandatory(lessonItem.getIsMandatory() == null || lessonItem.getIsMandatory());
                lesson.setMetadata(lessonItem.getMetadata());
                CourseLesson savedLesson = lessonRepository.save(lesson);
                retainedLessonIds.add(savedLesson.getId());
            }
        }

        for (CourseLesson lesson : existingLessons) {
            if (!retainedLessonIds.contains(lesson.getId())) {
                lessonRepository.delete(lesson);
            }
        }
        for (CourseSection section : existingSections) {
            if (!retainedSectionIds.contains(section.getId())) {
                sectionRepository.delete(section);
            }
        }

        return CourseCurriculumResponse.builder()
                .sections(mapper.toSectionResponseList(sectionRepository.findByCourseIdOrderBySectionOrderAsc(courseId)))
                .lessons(mapper.toLessonResponseList(lessonRepository.findAllByCourseId(courseId)))
                .build();
    }

    private void validateLessonContent(CourseCurriculumSaveRequest.LessonItem lesson) {
        if (lesson.getLessonType() == LessonType.DOWNLOADABLE && isBlank(lesson.getResourceUrl())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Downloadable lessons require a resource URL");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
