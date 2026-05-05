package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.exception.ErrorCode;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.feature.course.dto.request.LessonProgressUpdateRequest;
import com.mentorx.api.feature.course.dto.response.LessonProgressResponse;
import com.mentorx.api.feature.course.entity.CourseEnrollment;
import com.mentorx.api.feature.course.entity.CourseLesson;
import com.mentorx.api.feature.course.entity.LessonProgress;
import com.mentorx.api.feature.course.entity.LessonProgressId;
import com.mentorx.api.feature.course.mapper.CourseMapper;
import com.mentorx.api.feature.course.repository.CourseEnrollmentRepository;
import com.mentorx.api.feature.course.repository.CourseLessonRepository;
import com.mentorx.api.feature.course.repository.LessonProgressRepository;
import com.mentorx.api.feature.course.service.LessonProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LessonProgressServiceImpl implements LessonProgressService {

    private final LessonProgressRepository progressRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseLessonRepository lessonRepository;
    private final CourseMapper mapper;

    @Override
    @Transactional
    public LessonProgressResponse createOrUpdateProgress(UUID enrollmentId, UUID lessonId, LessonProgressUpdateRequest request) {
        log.info("Creating or updating progress for enrollment: {} and lesson: {}", enrollmentId, lessonId);

        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        CourseLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        LessonProgressId progressId = LessonProgressId.builder()
                .enrollmentId(enrollmentId)
                .lessonId(lessonId)
                .build();

        LessonProgress progress = progressRepository.findById(progressId)
                .orElse(LessonProgress.builder()
                        .id(progressId)
                        .enrollment(enrollment)
                        .lesson(lesson)
                        .isCompleted(false)
                        .watchDurationSec(0)
                        .build());

        if (request.getIsCompleted() != null) {
            progress.setIsCompleted(request.getIsCompleted());
            if (request.getIsCompleted() && progress.getCompletedAt() == null) {
                progress.setCompletedAt(LocalDateTime.now());
            }
        }

        if (request.getWatchDurationSec() != null) {
            progress.setWatchDurationSec(request.getWatchDurationSec());
        }

        LessonProgress savedProgress = progressRepository.save(progress);
        log.info("Lesson progress saved successfully");

        return mapper.toResponse(savedProgress);
    }

    @Override
    public LessonProgressResponse getProgress(UUID enrollmentId, UUID lessonId) {
        log.debug("Fetching progress for enrollment: {} and lesson: {}", enrollmentId, lessonId);
        
        LessonProgress progress = progressRepository.findByIdEnrollmentIdAndIdLessonId(enrollmentId, lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_PROGRESS_NOT_FOUND));

        return mapper.toResponse(progress);
    }

    @Override
    public List<LessonProgressResponse> getProgressByEnrollmentId(UUID enrollmentId) {
        log.debug("Fetching all progress for enrollment: {}", enrollmentId);
        
        List<LessonProgress> progressList = progressRepository.findByIdEnrollmentId(enrollmentId);
        return mapper.toProgressResponseList(progressList);
    }

    @Override
    public List<LessonProgressResponse> getProgressByStudentAndCourse(UUID studentId, UUID courseId) {
        log.debug("Fetching progress for student: {} and course: {}", studentId, courseId);
        
        List<LessonProgress> progressList = progressRepository.findByStudentIdAndCourseId(studentId, courseId);
        return mapper.toProgressResponseList(progressList);
    }

    @Override
    @Transactional
    public void markLessonAsCompleted(UUID enrollmentId, UUID lessonId) {
        log.info("Marking lesson as completed for enrollment: {} and lesson: {}", enrollmentId, lessonId);

        LessonProgressUpdateRequest request = LessonProgressUpdateRequest.builder()
                .isCompleted(true)
                .build();

        createOrUpdateProgress(enrollmentId, lessonId, request);
    }

    @Override
    public Long countCompletedLessons(UUID enrollmentId) {
        log.debug("Counting completed lessons for enrollment: {}", enrollmentId);
        return progressRepository.countCompletedLessonsByEnrollmentId(enrollmentId);
    }

    @Override
    public Long countTotalLessons(UUID enrollmentId) {
        log.debug("Counting total lessons for enrollment: {}", enrollmentId);
        return progressRepository.countTotalLessonsByEnrollmentId(enrollmentId);
    }
}
