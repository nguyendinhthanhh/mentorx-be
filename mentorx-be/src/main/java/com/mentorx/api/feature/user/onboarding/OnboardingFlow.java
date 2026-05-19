package com.mentorx.api.feature.user.onboarding;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class OnboardingFlow {

    private static final List<OnboardingStepEnum> ORDER = List.of(
            OnboardingStepEnum.ROLE,
            OnboardingStepEnum.INTERESTS,
            OnboardingStepEnum.SKILLS,
            OnboardingStepEnum.PREFERENCES,
            OnboardingStepEnum.GOALS,
            OnboardingStepEnum.PROFILE
    );

    public static final Set<OnboardingStepEnum> STEPS_REQUIRING_PAYLOAD = EnumSet.copyOf(ORDER);

    private OnboardingFlow() {
    }

    public static OnboardingStepEnum nextAfter(OnboardingStepEnum completed) {
        int i = ORDER.indexOf(completed);
        if (i < 0 || i >= ORDER.size() - 1) {
            return OnboardingStepEnum.DONE;
        }
        return ORDER.get(i + 1);
    }
}
