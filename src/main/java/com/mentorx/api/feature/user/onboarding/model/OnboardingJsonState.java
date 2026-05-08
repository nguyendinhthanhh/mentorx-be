package com.mentorx.api.feature.user.onboarding.model;

import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingJsonState {

    private OnboardingStepEnum currentStep = OnboardingStepEnum.ROLE;

    private Set<OnboardingStepEnum> completedSteps = new LinkedHashSet<>();

    private OnboardingDraftPayload draft = new OnboardingDraftPayload();

    public static OnboardingJsonState fresh() {
        return new OnboardingJsonState();
    }
}
