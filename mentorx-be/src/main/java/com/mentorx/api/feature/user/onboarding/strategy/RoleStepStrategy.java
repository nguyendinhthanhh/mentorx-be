package com.mentorx.api.feature.user.onboarding.strategy;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import com.mentorx.api.feature.user.onboarding.dto.request.BaseStepRequest;
import com.mentorx.api.feature.user.onboarding.dto.request.RoleStepRequest;
import com.mentorx.api.feature.user.onboarding.model.OnboardingJsonState;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RoleStepStrategy extends AbstractOnboardingStepStrategy {

    private static final Set<String> ALLOWED = Set.of("MENTOR", "CLIENT", "BOTH", "USER");

    @Override
    public OnboardingStepEnum step() {
        return OnboardingStepEnum.ROLE;
    }

    @Override
    public void apply(User user, OnboardingJsonState state, BaseStepRequest request) {
        assertExpectedStep(state, OnboardingStepEnum.ROLE);
        if (!(request instanceof RoleStepRequest r)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid payload for ROLE step");
        }
        String normalized = r.getRoleChoice() == null ? "" : r.getRoleChoice().trim().toUpperCase();
        if (!ALLOWED.contains(normalized)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "roleChoice must be one of: MENTOR, CLIENT, BOTH, USER");
        }
        state.getDraft().setRoleChoice(normalized);
        advance(state, OnboardingStepEnum.ROLE);
    }
}
