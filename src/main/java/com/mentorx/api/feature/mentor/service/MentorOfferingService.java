package com.mentorx.api.feature.mentor.service;

import com.mentorx.api.feature.mentor.dto.request.MentorOfferingRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorOfferingResponse;

import java.util.List;
import java.util.UUID;

public interface MentorOfferingService {

    MentorOfferingResponse createCourse(UUID userId, MentorOfferingRequest request);

    MentorOfferingResponse updateCourse(UUID courseId, MentorOfferingRequest request);

    void deleteCourse(UUID courseId);

    MentorOfferingResponse publishCourse(UUID courseId);

    MentorOfferingResponse archiveCourse(UUID courseId);

    MentorOfferingResponse getCourseById(UUID courseId);

    List<MentorOfferingResponse> getAllCoursesByMentor(UUID userId);

    List<MentorOfferingResponse> getPublishedCoursesByMentor(UUID userId);
}

