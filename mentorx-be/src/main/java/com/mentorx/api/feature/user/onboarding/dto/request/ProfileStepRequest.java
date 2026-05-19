package com.mentorx.api.feature.user.onboarding.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProfileStepRequest extends BaseStepRequest {

    @NotBlank
    private String displayName;

    private String avatarUrl;
}
