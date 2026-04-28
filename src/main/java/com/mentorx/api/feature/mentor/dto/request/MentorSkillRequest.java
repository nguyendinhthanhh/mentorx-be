package com.mentorx.api.feature.mentor.dto.request;

import jakarta.validation.constraints.*;

public record MentorSkillRequest(
        @NotBlank(message = "Skill name is required")
        @Size(max = 100, message = "Skill name must not exceed 100 characters")
        String skillName,

        @NotNull(message = "Proficiency level is required")
        @Min(value = 1, message = "Proficiency level must be between 1 and 5")
        @Max(value = 5, message = "Proficiency level must be between 1 and 5")
        Integer proficiencyLevel,

        @Min(value = 0, message = "Years of experience must be non-negative")
        @Max(value = 50, message = "Years of experience must not exceed 50")
        Integer yearsOfExperience,

        Boolean isPrimary
) {
}