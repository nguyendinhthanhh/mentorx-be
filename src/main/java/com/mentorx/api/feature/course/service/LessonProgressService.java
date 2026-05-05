package com.mentorx.api.feature.course.service;

import com.mentorx.api.feature.course.dto.request.LessonProgressUpdateRequest;
import com.mentorx.api.feature.course.dto.response.LessonProgressResponse;

import java.util.List;
import java.util.UUID;

public interface LessonProgressService {
    
    LessonProgressResponse createOrUpdateProgress(UUID enrollmentId, UUID lessonId, LessonProgressUpdateRequest request);
    
    LessonProgressResponse getProgress(UUID enrollmentId, UUID lessonId);
    
    List<LessonProgressResponse> getProgressByEnrollmentId(UUID enrollmentId);
    
    List<LessonProgressResponse> getProgressByStudentAndCourse(UUID studentId, UUID courseId);
    
    void markLessonAsCompleted(UUID enrollmentId, UUID lessonId);
    
    Long countCompletedLessons(UUID enrollmentId);
    
    Long countTotalLessons(UUID enrollmentId);
}
