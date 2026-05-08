package com.mentorx.api.feature.course.service;

import com.mentorx.api.feature.user.entity.User;

import java.util.UUID;

public interface CourseDocumentService {
    CourseDocumentPayload getPreview(UUID lessonId, User viewer);
    CourseDocumentPayload getDownload(UUID lessonId, User viewer);
}
