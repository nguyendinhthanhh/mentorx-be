package com.mentorx.api.feature.mentor.dto.response;

import com.mentorx.api.common.enums.MentorStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MentorProfileResponse(
        UUID id,
        UUID userId,
        String userFullName,
        String userDisplayName,
        String userAvatarUrl,
        String professionalTitle,
        Integer yearsOfExperience,
        BigDecimal hourlyRate,
        String expertiseSummary,
        String teachingApproach,
        String availabilityNote,
        String linkedinUrl,
        String githubUrl,
        String portfolioUrl,
        String videoIntroUrl,
        Boolean isAvailableForJobs,
        Boolean isAvailableForCourses,
        Integer maxConcurrentStudents,
        Integer responseTimeHours,
        MentorStatus status,
        String applicationNote,
        UUID approvedBy,
        String approvedByName,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt,
        List<MentorSkillResponse> skills,
        List<MentorAvailabilityResponse> availabilities,
        List<MentorCertificationResponse> certifications,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}