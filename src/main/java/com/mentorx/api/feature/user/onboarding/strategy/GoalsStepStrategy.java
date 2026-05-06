package com.mentorx.api.feature.user.onboarding.strategy;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import com.mentorx.api.feature.user.onboarding.dto.request.BaseStepRequest;
import com.mentorx.api.feature.user.onboarding.dto.request.GoalsStepRequest;
import com.mentorx.api.feature.user.onboarding.model.OnboardingJsonState;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class GoalsStepStrategy extends AbstractOnboardingStepStrategy {

    @Override
    public OnboardingStepEnum step() {
        return OnboardingStepEnum.GOALS;
    }

    @Override
    public void apply(User user, OnboardingJsonState state, BaseStepRequest request) {
        assertExpectedStep(state, OnboardingStepEnum.GOALS);
        if (!(request instanceof GoalsStepRequest r)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid payload for GOALS step");
        }
        state.getDraft().setGoals(new ArrayList<>(r.getGoals()));
        advance(state, OnboardingStepEnum.GOALS);
    }
}
