package com.mentorx.api.feature.user.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingDraftPayload {

    private String roleChoice;

    @Builder.Default
    private List<Integer> categoryIds = new ArrayList<>();

    @Builder.Default
    private List<SkillDraftItem> skills = new ArrayList<>();

    private PreferencesDraft preferences;

    @Builder.Default
    private List<String> goals = new ArrayList<>();

    private ProfileDraft profile;
}
