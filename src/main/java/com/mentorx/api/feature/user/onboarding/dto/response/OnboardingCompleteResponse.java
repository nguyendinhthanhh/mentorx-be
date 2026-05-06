package com.mentorx.api.feature.user.onboarding.dto.response;

import java.util.UUID;

public record OnboardingCompleteResponse(UUID userId, boolean finalized) {
}
