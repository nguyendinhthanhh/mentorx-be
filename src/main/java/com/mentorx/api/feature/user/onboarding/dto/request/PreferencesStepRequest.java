package com.mentorx.api.feature.user.onboarding.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PreferencesStepRequest extends BaseStepRequest {

    @NotNull
    private Boolean emailEnabled;

    @NotNull
    private Boolean pushEnabled;

    @NotNull
    private Boolean inAppEnabled;
}
