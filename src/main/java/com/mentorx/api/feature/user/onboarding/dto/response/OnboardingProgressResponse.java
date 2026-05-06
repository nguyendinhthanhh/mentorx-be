package com.mentorx.api.feature.user.onboarding.dto.response;

import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import com.mentorx.api.feature.user.onboarding.model.OnboardingDraftPayload;
import com.mentorx.api.feature.user.onboarding.model.OnboardingJsonState;

import java.util.LinkedHashSet;
import java.util.Set;

public record OnboardingProgressResponse(
        boolean onboarded,
        OnboardingStepEnum currentStep,
        Set<OnboardingStepEnum> completedSteps,
        OnboardingDraftPayload draft
) {
    public static OnboardingProgressResponse forCompletedUser() {
        return new OnboardingProgressResponse(true, null, Set.of(), null);
    }

    public static OnboardingProgressResponse inProgress(OnboardingJsonState state) {
        return new OnboardingProgressResponse(
                false,
                state.getCurrentStep(),
                new LinkedHashSet<>(state.getCompletedSteps()),
                state.getDraft()
        );
    }
}
