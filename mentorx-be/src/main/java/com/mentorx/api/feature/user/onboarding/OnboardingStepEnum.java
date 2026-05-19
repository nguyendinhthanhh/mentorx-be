package com.mentorx.api.feature.user.onboarding;

/**
 * Ordered onboarding steps. {@link #DONE} is only used as machine state after the last step payload.
 */
public enum OnboardingStepEnum {
    ROLE,
    INTERESTS,
    SKILLS,
    PREFERENCES,
    GOALS,
    PROFILE,
    DONE
}
