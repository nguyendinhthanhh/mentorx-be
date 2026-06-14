package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.feature.course.dto.request.CourseCurriculumSaveRequest;
import com.mentorx.api.feature.course.dto.response.CourseCurriculumResponse;
import com.mentorx.api.feature.course.dto.response.QuizQuestionResponse;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.entity.CourseLesson;
import com.mentorx.api.feature.course.entity.CourseSection;
import com.mentorx.api.feature.course.entity.QuizQuestion;
import com.mentorx.api.feature.course.mapper.CourseMapper;
import com.mentorx.api.feature.course.repository.CourseLessonRepository;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.course.repository.CourseSectionRepository;
import com.mentorx.api.feature.course.repository.QuizQuestionRepository;
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
    private final QuizQuestionRepository quizQuestionRepository;

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
        List<QuizQuestion> savedQuizQuestions = new ArrayList<>();

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
                lesson.setLessonType(normalizeLessonType(lessonItem.getLessonType()));
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
                savedQuizQuestions.addAll(saveQuizQuestions(savedLesson, lessonItem));
            }
        }

        for (CourseLesson lesson : existingLessons) {
            if (!retainedLessonIds.contains(lesson.getId())) {
                quizQuestionRepository.deleteAll(quizQuestionRepository.findByLessonIdOrderByOrderIndexAsc(lesson.getId()));
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
                .quizQuestions(savedQuizQuestions.stream().map(this::toQuizQuestionResponse).toList())
                .build();
    }

    private List<QuizQuestion> saveQuizQuestions(CourseLesson lesson, CourseCurriculumSaveRequest.LessonItem lessonItem) {
        List<QuizQuestion> existingQuestions = quizQuestionRepository.findByLessonIdOrderByOrderIndexAsc(lesson.getId());
        if (lesson.getLessonType() != com.mentorx.api.common.enums.LessonType.QUIZ) {
            if (!existingQuestions.isEmpty()) {
                quizQuestionRepository.deleteAll(existingQuestions);
            }
            return List.of();
        }

        List<CourseCurriculumSaveRequest.QuizQuestionItem> questionItems = lessonItem.getQuizQuestions() == null
                ? List.of()
                : lessonItem.getQuizQuestions();
        Map<UUID, QuizQuestion> existingById = existingQuestions.stream()
                .collect(Collectors.toMap(QuizQuestion::getId, question -> question));
        Set<UUID> retainedQuestionIds = new HashSet<>();
        List<QuizQuestion> savedQuestions = new ArrayList<>();

        for (CourseCurriculumSaveRequest.QuizQuestionItem item : questionItems) {
            QuizQuestion question = item.getId() == null ? new QuizQuestion() : existingById.get(item.getId());
            if (question == null) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Quiz question not found");
            }
            question.setLesson(lesson);
            question.setQuestionType(item.getQuestionType());
            question.setQuestionText(item.getQuestionText());
            question.setAnswerDataJson(item.getAnswerDataJson());
            question.setPoints(item.getPoints() == null ? 1 : item.getPoints());
            question.setExplanation(blankToNull(item.getExplanation()));
            question.setOrderIndex(item.getOrderIndex() == null ? savedQuestions.size() + 1 : item.getOrderIndex());
            QuizQuestion savedQuestion = quizQuestionRepository.save(question);
            retainedQuestionIds.add(savedQuestion.getId());
            savedQuestions.add(savedQuestion);
        }

        for (QuizQuestion existingQuestion : existingQuestions) {
            if (!retainedQuestionIds.contains(existingQuestion.getId())) {
                quizQuestionRepository.delete(existingQuestion);
            }
        }
        return savedQuestions;
    }

    private QuizQuestionResponse toQuizQuestionResponse(QuizQuestion question) {
        return QuizQuestionResponse.builder()
                .id(question.getId())
                .lessonId(question.getLesson().getId())
                .questionType(question.getQuestionType())
                .questionText(question.getQuestionText())
                .answerDataJson(question.getAnswerDataJson())
                .points(question.getPoints())
                .explanation(question.getExplanation())
                .orderIndex(question.getOrderIndex())
                .build();
    }

    private void validateLessonContent(CourseCurriculumSaveRequest.LessonItem lesson) {
        if (lesson.getLessonType() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Lesson type is required");
        }
    }

    private com.mentorx.api.common.enums.LessonType normalizeLessonType(com.mentorx.api.common.enums.LessonType lessonType) {
        return lessonType == com.mentorx.api.common.enums.LessonType.QUIZ
                ? com.mentorx.api.common.enums.LessonType.QUIZ
                : com.mentorx.api.common.enums.LessonType.LESSON;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
