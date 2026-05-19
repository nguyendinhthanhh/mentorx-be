package com.mentorx.api.feature.course.service;

import com.mentorx.api.feature.course.dto.request.CourseLessonCreateRequest;
import com.mentorx.api.feature.course.dto.request.CourseLessonUpdateRequest;
import com.mentorx.api.feature.course.dto.response.CourseLessonResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CourseLessonService {
    
    CourseLessonResponse createLesson(CourseLessonCreateRequest request);
    
    CourseLessonResponse getLessonById(UUID id);
    
    List<CourseLessonResponse> getLessonsBySectionId(UUID sectionId);
    
    Page<CourseLessonResponse> getLessonsBySectionId(UUID sectionId, Pageable pageable);
    
    List<CourseLessonResponse> getAllLessonsByCourseId(UUID courseId);
    
    CourseLessonResponse updateLesson(UUID id, CourseLessonUpdateRequest request);
    
    void deleteLesson(UUID id);
    
    Long countLessonsBySectionId(UUID sectionId);
    
    List<CourseLessonResponse> getFreePreviewLessonsByCourseId(UUID courseId);
    
    void incrementViewCount(UUID lessonId);
}
