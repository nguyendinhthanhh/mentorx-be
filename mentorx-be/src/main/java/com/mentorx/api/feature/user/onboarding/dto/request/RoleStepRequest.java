package com.mentorx.api.feature.user.onboarding.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleStepRequest extends BaseStepRequest {

    /**
     * e.g. MENTOR, CLIENT, BOTH
     */
    @NotBlank
    private String roleChoice;
}
