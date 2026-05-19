package com.mentorx.api.feature.user.onboarding.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SkillStepRequest extends BaseStepRequest {

    @NotNull
    @Valid
    private List<@Valid SkillItemRequest> skills;
}
