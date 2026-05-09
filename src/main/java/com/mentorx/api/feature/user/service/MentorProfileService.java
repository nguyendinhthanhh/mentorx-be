package com.mentorx.api.feature.user.service;

import com.mentorx.api.feature.user.dto.request.MentorProfileRequest;
import com.mentorx.api.feature.user.dto.response.MentorProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface MentorProfileService {

    MentorProfileResponse createMentorProfile(UUID userId, MentorProfileRequest request);

    MentorProfileResponse getMentorProfileByUserId(UUID userId);

    MentorProfileResponse updateMentorProfile(UUID userId, MentorProfileRequest request);

    void deleteMentorProfile(UUID userId);

    Page<MentorProfileResponse> getAllApprovedMentors(Pageable pageable);

    Page<MentorProfileResponse> getPendingMentorApplications(Pageable pageable);

    Page<MentorProfileResponse> getMentorsWithFilters(BigDecimal minRating, BigDecimal maxHourlyRate, 
                                                     String availability, Pageable pageable);

    List<MentorProfileResponse> getFeaturedMentors();

    Page<MentorProfileResponse> getTopRatedMentors(Pageable pageable);

    List<MentorProfileResponse> searchMentors(String searchQuery);

    MentorProfileResponse approveMentorApplication(UUID userId, UUID approvedBy);

    MentorProfileResponse rejectMentorApplication(UUID userId, String rejectionReason, UUID rejectedBy);

    MentorProfileResponse requestMentorApplicationRevision(UUID userId, String revisionReason, UUID requestedBy);

    void setFeaturedStatus(UUID userId, boolean featured);

    boolean isMentorSaved(UUID userId, UUID mentorUserId);

    boolean saveMentor(UUID userId, UUID mentorUserId);

    boolean unsaveMentor(UUID userId, UUID mentorUserId);

    List<MentorProfileResponse> getSavedMentors(UUID userId);

    long getApprovedMentorsCount();

    long getPendingApplicationsCount();
}
