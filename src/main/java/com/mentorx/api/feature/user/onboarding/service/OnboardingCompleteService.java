package com.mentorx.api.feature.user.onboarding.service;

import com.mentorx.api.common.enums.UserStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.matching.entity.UserInterestProfile;
import com.mentorx.api.feature.matching.repository.UserInterestProfileRepository;
import com.mentorx.api.feature.system.entity.NotificationPreference;
import com.mentorx.api.feature.system.entity.UserSkill;
import com.mentorx.api.feature.system.repository.CategoryRepository;
import com.mentorx.api.feature.system.repository.NotificationPreferenceRepository;
import com.mentorx.api.feature.system.repository.SkillRepository;
import com.mentorx.api.feature.system.repository.UserSkillRepository;
import com.mentorx.api.feature.user.entity.Role;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserRole;
import com.mentorx.api.feature.user.onboarding.OnboardingFlow;
import com.mentorx.api.feature.user.onboarding.OnboardingSecuritySupport;
import com.mentorx.api.feature.user.onboarding.OnboardingStepEnum;
import com.mentorx.api.feature.user.onboarding.dto.response.OnboardingCompleteResponse;
import com.mentorx.api.feature.user.onboarding.event.OnboardingCompletedEvent;
import com.mentorx.api.feature.user.onboarding.model.OnboardingDraftPayload;
import com.mentorx.api.feature.user.onboarding.model.OnboardingJsonState;
import com.mentorx.api.feature.user.onboarding.model.PreferencesDraft;
import com.mentorx.api.feature.user.onboarding.model.ProfileDraft;
import com.mentorx.api.feature.user.onboarding.model.SkillDraftItem;
import com.mentorx.api.feature.user.repository.RoleRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingCompleteService {

    private final UserRepository userRepository;
    private final OnboardingSecuritySupport onboardingSecuritySupport;
    private final CategoryRepository categoryRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserInterestProfileRepository userInterestProfileRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OnboardingCompleteResponse complete() {
        User user = onboardingSecuritySupport.requireCurrentUser();
        user = userRepository.findById(user.getId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsOnboarded())) {
            throw new AppException(ErrorCode.ONBOARDING_ALREADY_COMPLETED);
        }

        OnboardingJsonState state = user.getOnboardingState();
        if (state == null || state.getCurrentStep() != OnboardingStepEnum.DONE) {
            throw new AppException(ErrorCode.ONBOARDING_NOT_READY, "Complete all steps before calling /complete");
        }
        if (!state.getCompletedSteps().containsAll(OnboardingFlow.STEPS_REQUIRING_PAYLOAD)) {
            throw new AppException(ErrorCode.ONBOARDING_NOT_READY);
        }

        OnboardingDraftPayload draft = state.getDraft();
        validateDraft(draft);

        applyRoleAndRbac(user, draft);
        persistInterestProfiles(user, draft.getCategoryIds());
        persistSkills(user.getId(), draft.getSkills());
        persistNotificationPreferences(user.getId(), draft.getPreferences());
        applyGoalsToBio(user, draft.getGoals());
        applyProfile(user, draft.getProfile());

        user.setOnboardingState(null);
        user.setIsOnboarded(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        eventPublisher.publishEvent(new OnboardingCompletedEvent(user.getId()));
        log.info("Onboarding finalized for user {}", user.getId());
        return new OnboardingCompleteResponse(user.getId(), true);
    }

    private void validateDraft(OnboardingDraftPayload draft) {
        if (draft.getRoleChoice() == null || draft.getRoleChoice().isBlank()) {
            throw new AppException(ErrorCode.ONBOARDING_NOT_READY, "Missing role in draft");
        }
        if (draft.getCategoryIds() == null || draft.getCategoryIds().isEmpty()) {
            throw new AppException(ErrorCode.ONBOARDING_NOT_READY, "Missing interests in draft");
        }
    }

    private void applyRoleAndRbac(User user, OnboardingDraftPayload draft) {
        String rc = draft.getRoleChoice();
        boolean wantMentor = "MENTOR".equals(rc) || "BOTH".equals(rc);
        if (!wantMentor && user.getMentorStatus() != null && user.getMentorStatus().name().equals("APPROVED")) {
            user.setIsMentor(true);
        } else if (!wantMentor) {
            user.setIsMentor(false);
        }

        assignRoleIfMissing(user, "USER");
    }

    private void assignRoleIfMissing(User user, String roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found: " + roleName));
        if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
            userRoleRepository.save(UserRole.builder()
                    .userId(user.getId())
                    .roleId(role.getId())
                    .grantedAt(LocalDateTime.now())
                    .build());
        }
    }

    private void persistInterestProfiles(User user, List<Integer> categoryIds) {
        LocalDateTime now = LocalDateTime.now();
        for (Integer categoryId : categoryIds) {
            if (!categoryRepository.existsById(categoryId)) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found: " + categoryId);
            }
            UserInterestProfile profile = userInterestProfileRepository
                    .findByUserIdAndCategoryId(user.getId(), categoryId)
                    .orElseGet(UserInterestProfile::new);
            profile.setUser(user);
            profile.setCategory(categoryRepository.getReferenceById(categoryId));
            profile.setInterestScore(BigDecimal.ONE);
            if (profile.getInteractionCount() == null) {
                profile.setInteractionCount(0);
            }
            if (profile.getTimeSpentMinutes() == null) {
                profile.setTimeSpentMinutes(0);
            }
            profile.setLastInteractionAt(now);
            profile.setLastUpdated(now);
            if (profile.getDecayFactor() == null) {
                profile.setDecayFactor(new BigDecimal("0.9500"));
            }
            profile.setIsExplicit(true);
            userInterestProfileRepository.save(profile);
        }
    }

    private void persistSkills(java.util.UUID userId, List<SkillDraftItem> skills) {
        userSkillRepository.deleteByUserId(userId);
        if (skills == null) {
            return;
        }
        for (SkillDraftItem item : skills) {
            if (!skillRepository.existsById(item.getSkillId())) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Skill not found: " + item.getSkillId());
            }
            userSkillRepository.save(UserSkill.builder()
                    .userId(userId)
                    .skillId(item.getSkillId())
                    .level(item.getLevel())
                    .build());
        }
    }

    private void persistNotificationPreferences(java.util.UUID userId, PreferencesDraft draft) {
        PreferencesDraft prefs = draft != null ? draft : PreferencesDraft.builder().build();
        NotificationPreference entity = notificationPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> NotificationPreference.builder().userId(userId).build());
        entity.setEmailEnabled(prefs.getEmailEnabled() == null || prefs.getEmailEnabled());
        entity.setPushEnabled(prefs.getPushEnabled() == null || prefs.getPushEnabled());
        entity.setInAppEnabled(prefs.getInAppEnabled() == null || prefs.getInAppEnabled());
        if (entity.getEmailTypeSettings() == null) {
            entity.setEmailTypeSettings("{}");
        }
        if (entity.getPushTypeSettings() == null) {
            entity.setPushTypeSettings("{}");
        }
        entity.setUpdatedAt(LocalDateTime.now());
        notificationPreferenceRepository.save(entity);
    }

    private void applyGoalsToBio(User user, List<String> goals) {
        if (goals == null || goals.isEmpty()) {
            return;
        }
        String goalsText = "Learning goals: " + String.join(", ", goals);
        if (user.getBio() == null || user.getBio().isBlank()) {
            user.setBio(goalsText);
        } else {
            user.setBio(user.getBio() + "\n" + goalsText);
        }
    }

    private void applyProfile(User user, ProfileDraft profile) {
        if (profile == null) {
            return;
        }
        if (profile.getDisplayName() != null) {
            user.setDisplayName(profile.getDisplayName());
        }
        if (profile.getAvatarUrl() != null) {
            user.setAvatarUrl(profile.getAvatarUrl());
        }
    }
}
