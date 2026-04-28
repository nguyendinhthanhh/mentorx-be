package com.mentorx.api.feature.mentor.service;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.feature.mentor.dto.request.MentorApplicationRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface MentorService {

    MentorProfileResponse applyAsMentor(UUID userId, MentorApplicationRequest request);

    MentorProfileResponse getMentorProfile(UUID mentorProfileId);

    MentorProfileResponse getMentorProfileByUserId(UUID userId);

    MentorProfileResponse updateMentorProfile(UUID mentorProfileId, MentorApplicationRequest request);

    void deleteMentorProfile(UUID mentorProfileId);

    MentorProfileResponse approveMentorApplication(UUID mentorProfileId, UUID approvedBy, String adminNotes);

    MentorProfileResponse rejectMentorApplication(UUID mentorProfileId, UUID rejectedBy, String adminNotes);

    MentorProfileResponse updateMentorStatus(UUID mentorProfileId, MentorStatus status);

    Page<MentorProfileResponse> getAllMentors(Pageable pageable);

    Page<MentorProfileResponse> getMentorsByStatus(MentorStatus status, Pageable pageable);

    Page<MentorProfileResponse> getAvailableMentorsForJobs(Pageable pageable);

    Page<MentorProfileResponse> getAvailableMentorsForCourses(Pageable pageable);

    List<MentorProfileResponse> searchMentorsBySkill(String skillName);

    List<MentorProfileResponse> getMentorsByHourlyRateRange(BigDecimal minRate, BigDecimal maxRate);

    Page<MentorProfileResponse> getMentorsWithFilters(String skillName, BigDecimal minRate, BigDecimal maxRate,
                                                     Integer minExperience, Boolean availableForJobs, Pageable pageable);

    List<MentorProfileResponse> searchMentors(String searchTerm);

    List<MentorProfileResponse> getMentorsByMaxResponseTime(Integer maxResponseTime);

    long getTotalMentorsCount();

    long getApprovedMentorsCount();

    long getPendingApplicationsCount();

    List<String> getPopularSkills();

    List<String> searchSkills(String skillName);

    void updateMentorAvailability(UUID mentorProfileId, Boolean availableForJobs, Boolean availableForCourses);
}