package com.mentorx.api.feature.user.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record UserPreferenceResponse(
        UUID userId,
        List<Integer> interestedDomainIds,
        List<Integer> preferredSkillIds,
        List<String> learningGoals,
        List<String> preferredLanguages,
        Boolean onboardingCompleted
) {}

