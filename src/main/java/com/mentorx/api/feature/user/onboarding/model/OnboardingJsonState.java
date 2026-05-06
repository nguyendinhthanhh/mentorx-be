package com.mentorx.api.feature.user.onboarding.model;

import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingJsonState {

    @Builder.Default
    private OnboardingStepEnum currentStep = OnboardingStepEnum.ROLE;

    @Builder.Default
    private Set<OnboardingStepEnum> completedSteps = new LinkedHashSet<>();

    @Builder.Default
    private OnboardingDraftPayload draft = new OnboardingDraftPayload();

    public static OnboardingJsonState fresh() {
        return OnboardingJsonState.builder()
                .currentStep(OnboardingStepEnum.ROLE)
                .completedSteps(new LinkedHashSet<>())
                .draft(new OnboardingDraftPayload())
                .build();
    }
}
