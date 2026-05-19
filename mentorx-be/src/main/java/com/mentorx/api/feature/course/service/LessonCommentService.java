package com.mentorx.api.feature.course.service;

import com.mentorx.api.feature.course.dto.request.LessonCommentCreateRequest;
import com.mentorx.api.feature.course.dto.request.LessonCommentUpdateRequest;
import com.mentorx.api.feature.course.dto.response.LessonCommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface LessonCommentService {
    
    LessonCommentResponse createComment(LessonCommentCreateRequest request, UUID userId);
    
    LessonCommentResponse getCommentById(UUID id);
    
    Page<LessonCommentResponse> getCommentsByLessonId(UUID lessonId, Pageable pageable);
    
    List<LessonCommentResponse> getRepliesByParentId(UUID parentId);
    
    Page<LessonCommentResponse> getCommentsByUserId(UUID userId, Pageable pageable);
    
    LessonCommentResponse updateComment(UUID id, LessonCommentUpdateRequest request, UUID userId);
    
    void deleteComment(UUID id, UUID userId);
    
    Long countCommentsByLessonId(UUID lessonId);
}
