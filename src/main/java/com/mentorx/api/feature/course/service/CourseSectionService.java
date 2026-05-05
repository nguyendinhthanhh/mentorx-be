package com.mentorx.api.feature.course.service;

import com.mentorx.api.feature.course.dto.request.CourseSectionCreateRequest;
import com.mentorx.api.feature.course.dto.request.CourseSectionUpdateRequest;
import com.mentorx.api.feature.course.dto.response.CourseSectionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CourseSectionService {
    
    CourseSectionResponse createSection(CourseSectionCreateRequest request);
    
    CourseSectionResponse getSectionById(UUID id);
    
    List<CourseSectionResponse> getSectionsByCourseId(UUID courseId);
    
    Page<CourseSectionResponse> getSectionsByCourseId(UUID courseId, Pageable pageable);
    
    CourseSectionResponse updateSection(UUID id, CourseSectionUpdateRequest request);
    
    void deleteSection(UUID id);
    
    Long countSectionsByCourseId(UUID courseId);
    
    List<CourseSectionResponse> getPublishedSectionsByCourseId(UUID courseId);
}
