package com.mentorx.api.feature.user.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferencesDraft {
    @Builder.Default
    private Boolean emailEnabled = true;
    @Builder.Default
    private Boolean pushEnabled = true;
    @Builder.Default
    private Boolean inAppEnabled = true;
}
