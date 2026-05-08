package com.mentorx.api.feature.user.onboarding.strategy;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.onboarding.OnboardingFlow;
import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import com.mentorx.api.feature.user.onboarding.model.OnboardingJsonState;

abstract class AbstractOnboardingStepStrategy implements OnboardingStepStrategy {

    protected void assertExpectedStep(OnboardingJsonState state, OnboardingStepEnum requested) {
        if (state.getCurrentStep() != requested && !state.getCompletedSteps().contains(requested)) {
            throw new AppException(ErrorCode.ONBOARDING_INVALID_STEP,
                    "Expected step " + state.getCurrentStep() + " but received " + requested);
        }
    }

    protected void advance(OnboardingJsonState state, OnboardingStepEnum completed) {
        state.getCompletedSteps().add(completed);
        state.setCurrentStep(OnboardingFlow.nextAfter(completed));
    }
}
