package com.mentorx.api.feature.user.onboarding.strategy;

import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import com.mentorx.api.feature.user.onboarding.dto.request.BaseStepRequest;
import com.mentorx.api.feature.user.onboarding.model.OnboardingJsonState;

public interface OnboardingStepStrategy {

    OnboardingStepEnum step();

    void apply(User user, OnboardingJsonState state, BaseStepRequest request);
}
