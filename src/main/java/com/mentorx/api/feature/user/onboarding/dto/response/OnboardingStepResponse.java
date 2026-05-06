package com.mentorx.api.feature.user.onboarding.dto.response;

import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;

import java.util.Set;

public record OnboardingStepResponse(
        OnboardingStepEnum currentStep,
        Set<OnboardingStepEnum> completedSteps,
        String message
) {
}
