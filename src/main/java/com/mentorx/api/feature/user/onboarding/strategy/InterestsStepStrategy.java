package com.mentorx.api.feature.user.onboarding.strategy;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import com.mentorx.api.feature.user.onboarding.dto.request.BaseStepRequest;
import com.mentorx.api.feature.user.onboarding.dto.request.InterestsStepRequest;
import com.mentorx.api.feature.user.onboarding.model.OnboardingJsonState;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class InterestsStepStrategy extends AbstractOnboardingStepStrategy {

    @Override
    public OnboardingStepEnum step() {
        return OnboardingStepEnum.INTERESTS;
    }

    @Override
    public void apply(User user, OnboardingJsonState state, BaseStepRequest request) {
        assertExpectedStep(state, OnboardingStepEnum.INTERESTS);
        if (!(request instanceof InterestsStepRequest r)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid payload for INTERESTS step");
        }
        state.getDraft().setCategoryIds(new ArrayList<>(r.getCategoryIds()));
        advance(state, OnboardingStepEnum.INTERESTS);
    }
}
