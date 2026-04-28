package com.mentorx.api.feature.mentor.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record MentorSkillResponse(
        UUID id,
        String skillName,
        Integer proficiencyLevel,
        Integer yearsOfExperience,
        Boolean isPrimary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}