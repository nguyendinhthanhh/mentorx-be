package com.mentorx.api.feature.user.onboarding.service;

import com.mentorx.api.common.enums.UserStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.onboarding.OnboardingSecuritySupport;
import com.mentorx.api.feature.user.onboarding.dto.request.BaseStepRequest;
import com.mentorx.api.feature.user.onboarding.dto.response.OnboardingProgressResponse;
import com.mentorx.api.feature.user.onboarding.dto.response.OnboardingStepResponse;
import com.mentorx.api.feature.user.onboarding.model.OnboardingJsonState;
import com.mentorx.api.feature.user.onboarding.strategy.OnboardingStepStrategyRegistry;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
public class OnboardingFlowService {

    private final UserRepository userRepository;
    private final OnboardingSecuritySupport onboardingSecuritySupport;
    private final OnboardingStepStrategyRegistry strategyRegistry;

    @Transactional(readOnly = true)
    public OnboardingProgressResponse getProgress() {
        User user = onboardingSecuritySupport.requireCurrentUser();
        if (Boolean.TRUE.equals(user.getIsOnboarded())) {
            return OnboardingProgressResponse.forCompletedUser();
        }
        OnboardingJsonState state = user.getOnboardingState() != null
                ? user.getOnboardingState()
                : OnboardingJsonState.fresh();
        return OnboardingProgressResponse.inProgress(state);
    }

    @Transactional
    public OnboardingStepResponse processStep(BaseStepRequest request) {
        User user = onboardingSecuritySupport.requireCurrentUser();
        if (Boolean.TRUE.equals(user.getIsOnboarded())) {
            throw new AppException(ErrorCode.ONBOARDING_ALREADY_COMPLETED);
        }
        OnboardingJsonState state = user.getOnboardingState() != null
                ? user.getOnboardingState()
                : OnboardingJsonState.fresh();
        strategyRegistry.get(request.getStepEnum()).apply(user, state, request);
        user.setOnboardingState(state);
        userRepository.save(user);
        return new OnboardingStepResponse(
                state.getCurrentStep(),
                new LinkedHashSet<>(state.getCompletedSteps()),
                "Step saved"
        );
    }

    @Transactional
    public void skip() {
        User user = onboardingSecuritySupport.requireCurrentUser();
        if (Boolean.TRUE.equals(user.getIsOnboarded())) {
            return;
        }
        if (user.getOnboardingState() == null) {
            user.setOnboardingState(OnboardingJsonState.fresh());
        }
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
        }
        user.setIsOnboarded(true);
        userRepository.save(user);
    }
}
