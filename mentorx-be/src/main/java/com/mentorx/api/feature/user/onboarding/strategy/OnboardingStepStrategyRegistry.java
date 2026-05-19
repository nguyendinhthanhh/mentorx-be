package com.mentorx.api.feature.user.onboarding.strategy;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OnboardingStepStrategyRegistry {

    private final Map<OnboardingStepEnum, OnboardingStepStrategy> strategies;

    public OnboardingStepStrategyRegistry(List<OnboardingStepStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(OnboardingStepStrategy::step, Function.identity()));
    }

    public OnboardingStepStrategy get(OnboardingStepEnum step) {
        OnboardingStepStrategy strategy = strategies.get(step);
        if (strategy == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Unsupported onboarding step: " + step);
        }
        return strategy;
    }
}
