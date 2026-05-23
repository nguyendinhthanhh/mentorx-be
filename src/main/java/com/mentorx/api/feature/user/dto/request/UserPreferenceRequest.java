package com.mentorx.api.feature.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record UserPreferenceRequest(
        List<Integer> interestedDomainIds,
        List<Integer> preferredSkillIds,
        @Size(max = 20) List<@Size(max = 120) String> learningGoals,
        @Size(max = 10) List<@Size(max = 50) String> preferredLanguages,
        Boolean onboardingCompleted
) {}

