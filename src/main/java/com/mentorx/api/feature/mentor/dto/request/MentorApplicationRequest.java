package com.mentorx.api.feature.mentor.dto.request;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

public record MentorApplicationRequest(
        @NotBlank(message = "Professional title is required")
        @Size(max = 200, message = "Professional title must not exceed 200 characters")
        String professionalTitle,

        @NotNull(message = "Years of experience is required")
        @Min(value = 0, message = "Years of experience must be non-negative")
        @Max(value = 50, message = "Years of experience must not exceed 50")
        Integer yearsOfExperience,

        @NotNull(message = "Hourly rate is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Hourly rate must be positive")
        @DecimalMax(value = "10000.0", message = "Hourly rate must not exceed 10000")
        BigDecimal hourlyRate,

        @NotBlank(message = "Expertise summary is required")
        @Size(max = 2000, message = "Expertise summary must not exceed 2000 characters")
        String expertiseSummary,

        @Size(max = 2000, message = "Teaching approach must not exceed 2000 characters")
        String teachingApproach,

        @Size(max = 1000, message = "Availability note must not exceed 1000 characters")
        String availabilityNote,

        @Pattern(regexp = "^https://.*linkedin\\.com/.*", message = "Invalid LinkedIn URL")
        String linkedinUrl,

        @Pattern(regexp = "^https://.*github\\.com/.*", message = "Invalid GitHub URL")
        String githubUrl,

        String portfolioUrl,

        String videoIntroUrl,

        Boolean isAvailableForJobs,

        Boolean isAvailableForCourses,

        @Min(value = 1, message = "Max concurrent students must be at least 1")
        @Max(value = 100, message = "Max concurrent students must not exceed 100")
        Integer maxConcurrentStudents,

        @Min(value = 1, message = "Response time must be at least 1 hour")
        @Max(value = 168, message = "Response time must not exceed 168 hours")
        Integer responseTimeHours,

        @Size(max = 2000, message = "Application note must not exceed 2000 characters")
        String applicationNote,

        @NotEmpty(message = "At least one skill is required")
        @Valid
        List<MentorSkillRequest> skills,

        @Valid
        List<MentorAvailabilityRequest> availabilities,

        @Valid
        List<MentorCertificationRequest> certifications
) {
}