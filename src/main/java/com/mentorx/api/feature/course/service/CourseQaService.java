package com.mentorx.api.feature.course.service;

import com.mentorx.api.feature.course.dto.request.CourseQaMessageRequest;
import com.mentorx.api.feature.course.dto.response.CourseQaMessageResponse;
import com.mentorx.api.feature.course.dto.response.CourseQaSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface CourseQaService {
    CourseQaMessageResponse send(UUID courseId, UUID senderId, CourseQaMessageRequest request);
    List<CourseQaMessageResponse> recent(UUID courseId, UUID userId);
    List<CourseQaSummaryResponse> unansweredSummariesForMentor(UUID mentorId);
    CourseQaSummaryResponse unansweredSummary(UUID courseId, UUID mentorId);
}
