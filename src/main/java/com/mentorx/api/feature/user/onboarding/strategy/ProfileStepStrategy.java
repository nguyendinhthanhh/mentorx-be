package com.mentorx.api.feature.user.onboarding.strategy;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import com.mentorx.api.feature.user.onboarding.dto.request.BaseStepRequest;
import com.mentorx.api.feature.user.onboarding.dto.request.ProfileStepRequest;
import com.mentorx.api.feature.user.onboarding.model.OnboardingJsonState;
import com.mentorx.api.feature.user.onboarding.model.ProfileDraft;
import org.springframework.stereotype.Component;

@Component
public class ProfileStepStrategy extends AbstractOnboardingStepStrategy {

    @Override
    public OnboardingStepEnum step() {
        return OnboardingStepEnum.PROFILE;
    }

    @Override
    public void apply(User user, OnboardingJsonState state, BaseStepRequest request) {
        assertExpectedStep(state, OnboardingStepEnum.PROFILE);
        if (!(request instanceof ProfileStepRequest r)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid payload for PROFILE step");
        }
        state.getDraft().setProfile(ProfileDraft.builder()
                .displayName(r.getDisplayName())
                .avatarUrl(r.getAvatarUrl())
                .build());
        advance(state, OnboardingStepEnum.PROFILE);
    }
}
