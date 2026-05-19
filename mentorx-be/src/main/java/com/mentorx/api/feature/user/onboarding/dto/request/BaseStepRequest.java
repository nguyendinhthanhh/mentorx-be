package com.mentorx.api.feature.user.onboarding.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "stepEnum", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RoleStepRequest.class, name = "ROLE"),
        @JsonSubTypes.Type(value = InterestsStepRequest.class, name = "INTERESTS"),
        @JsonSubTypes.Type(value = SkillStepRequest.class, name = "SKILLS"),
        @JsonSubTypes.Type(value = PreferencesStepRequest.class, name = "PREFERENCES"),
        @JsonSubTypes.Type(value = GoalsStepRequest.class, name = "GOALS"),
        @JsonSubTypes.Type(value = ProfileStepRequest.class, name = "PROFILE")
})
@Data
public abstract class BaseStepRequest {

    @NotNull
    private OnboardingStepEnum stepEnum;
}
