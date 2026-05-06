package com.mentorx.api.feature.user.onboarding.strategy;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import com.mentorx.api.feature.user.onboarding.dto.request.BaseStepRequest;
import com.mentorx.api.feature.user.onboarding.dto.request.SkillStepRequest;
import com.mentorx.api.feature.user.onboarding.model.OnboardingJsonState;
import com.mentorx.api.feature.user.onboarding.model.SkillDraftItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class SkillStepStrategy extends AbstractOnboardingStepStrategy {

    @Override
    public OnboardingStepEnum step() {
        return OnboardingStepEnum.SKILLS;
    }

    @Override
    public void apply(User user, OnboardingJsonState state, BaseStepRequest request) {
        assertExpectedStep(state, OnboardingStepEnum.SKILLS);
        if (!(request instanceof SkillStepRequest r)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid payload for SKILLS step");
        }
        var items = new ArrayList<SkillDraftItem>();
        for (var s : r.getSkills()) {
            items.add(SkillDraftItem.builder()
                    .skillId(s.getSkillId())
                    .level(s.getLevel())
                    .build());
        }
        state.getDraft().setSkills(items);
        advance(state, OnboardingStepEnum.SKILLS);
    }
}
