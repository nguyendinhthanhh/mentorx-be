package com.mentorx.api.feature.course.service;

import com.mentorx.api.feature.course.dto.request.CourseCurriculumSaveRequest;
import com.mentorx.api.feature.course.dto.response.CourseCurriculumResponse;

import java.util.UUID;

public interface CourseCurriculumService {
    CourseCurriculumResponse saveCurriculum(UUID courseId, CourseCurriculumSaveRequest request);
}
