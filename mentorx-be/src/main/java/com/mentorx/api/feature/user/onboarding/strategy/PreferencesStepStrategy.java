package com.mentorx.api.feature.user.onboarding.strategy;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import com.mentorx.api.feature.user.onboarding.dto.request.BaseStepRequest;
import com.mentorx.api.feature.user.onboarding.dto.request.PreferencesStepRequest;
import com.mentorx.api.feature.user.onboarding.model.OnboardingJsonState;
import com.mentorx.api.feature.user.onboarding.model.PreferencesDraft;
import org.springframework.stereotype.Component;

@Component
public class PreferencesStepStrategy extends AbstractOnboardingStepStrategy {

    @Override
    public OnboardingStepEnum step() {
        return OnboardingStepEnum.PREFERENCES;
    }

    @Override
    public void apply(User user, OnboardingJsonState state, BaseStepRequest request) {
        assertExpectedStep(state, OnboardingStepEnum.PREFERENCES);
        if (!(request instanceof PreferencesStepRequest r)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid payload for PREFERENCES step");
        }
        state.getDraft().setPreferences(PreferencesDraft.builder()
                .emailEnabled(r.getEmailEnabled())
                .pushEnabled(r.getPushEnabled())
                .inAppEnabled(r.getInAppEnabled())
                .build());
        advance(state, OnboardingStepEnum.PREFERENCES);
    }
}
