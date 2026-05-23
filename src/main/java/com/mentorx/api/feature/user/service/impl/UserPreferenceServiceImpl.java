package com.mentorx.api.feature.user.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.util.SecurityUtils;
import com.mentorx.api.feature.matching.entity.UserInterestProfile;
import com.mentorx.api.feature.matching.repository.UserInterestProfileRepository;
import com.mentorx.api.feature.system.entity.UserSkill;
import com.mentorx.api.feature.system.repository.CategoryRepository;
import com.mentorx.api.feature.system.repository.SkillRepository;
import com.mentorx.api.feature.system.repository.UserSkillRepository;
import com.mentorx.api.feature.user.dto.request.UserPreferenceRequest;
import com.mentorx.api.feature.user.dto.response.UserPreferenceResponse;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserPreference;
import com.mentorx.api.feature.user.repository.UserPreferenceRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final CategoryRepository categoryRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserInterestProfileRepository userInterestProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public UserPreferenceResponse getCurrentPreferences() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserPreference preference = userPreferenceRepository.findByUserId(userId).orElse(null);
        List<Integer> interestedDomainIds = preference != null
                ? dedupeIntegers(preference.getInterestedDomainIds())
                : userInterestProfileRepository.findByUserIdOrderByInterestScoreDesc(userId).stream()
                .map(profile -> profile.getCategory().getId())
                .collect(Collectors.toList());

        List<Integer> preferredSkillIds = preference != null
                ? dedupeIntegers(preference.getPreferredSkillIds())
                : userSkillRepository.findByUserId(userId).stream()
                .map(UserSkill::getSkillId)
                .collect(Collectors.toList());

        List<String> learningGoals = preference != null ? normalizeTextList(preference.getLearningGoals()) : new ArrayList<>();
        List<String> preferredLanguages = preference != null ? normalizeTextList(preference.getPreferredLanguages()) : defaultLanguage(user);

        return UserPreferenceResponse.builder()
                .userId(userId)
                .interestedDomainIds(interestedDomainIds)
                .preferredSkillIds(preferredSkillIds)
                .learningGoals(learningGoals)
                .preferredLanguages(preferredLanguages)
                .onboardingCompleted(preference != null ? Boolean.TRUE.equals(preference.getOnboardingCompleted()) : Boolean.TRUE.equals(user.getIsOnboarded()))
                .build();
    }

    @Override
    @Transactional
    public UserPreferenceResponse updateCurrentPreferences(UserPreferenceRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Integer> interestedDomainIds = dedupeIntegers(request.interestedDomainIds());
        List<Integer> preferredSkillIds = dedupeIntegers(request.preferredSkillIds());
        List<String> learningGoals = normalizeTextList(request.learningGoals());
        List<String> preferredLanguages = normalizeTextList(request.preferredLanguages());

        validateCategoryIds(interestedDomainIds);
        validateSkillIds(preferredSkillIds);

        UserPreference preference = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPreference.builder().user(user).build());
        preference.setInterestedDomainIds(interestedDomainIds);
        preference.setPreferredSkillIds(preferredSkillIds);
        preference.setLearningGoals(learningGoals);
        preference.setPreferredLanguages(preferredLanguages);
        preference.setOnboardingCompleted(
                request.onboardingCompleted() != null
                        ? request.onboardingCompleted()
                        : Boolean.TRUE.equals(preference.getOnboardingCompleted())
        );
        userPreferenceRepository.save(preference);

        if (Boolean.TRUE.equals(preference.getOnboardingCompleted()) && !Boolean.TRUE.equals(user.getIsOnboarded())) {
            user.setIsOnboarded(true);
            userRepository.save(user);
        }

        syncInterestedDomains(user, interestedDomainIds);
        syncPreferredSkills(userId, preferredSkillIds);

        return UserPreferenceResponse.builder()
                .userId(userId)
                .interestedDomainIds(interestedDomainIds)
                .preferredSkillIds(preferredSkillIds)
                .learningGoals(learningGoals)
                .preferredLanguages(preferredLanguages)
                .onboardingCompleted(Boolean.TRUE.equals(preference.getOnboardingCompleted()))
                .build();
    }

    private void validateCategoryIds(List<Integer> categoryIds) {
        for (Integer categoryId : categoryIds) {
            if (!categoryRepository.existsById(categoryId)) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found: " + categoryId);
            }
        }
    }

    private void validateSkillIds(List<Integer> skillIds) {
        for (Integer skillId : skillIds) {
            if (!skillRepository.existsById(skillId)) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Skill not found: " + skillId);
            }
        }
    }

    private void syncInterestedDomains(User user, List<Integer> interestedDomainIds) {
        List<UserInterestProfile> existingProfiles = userInterestProfileRepository.findByUserIdOrderByInterestScoreDesc(user.getId());
        List<UserInterestProfile> explicitProfiles = existingProfiles.stream()
                .filter(profile -> Boolean.TRUE.equals(profile.getIsExplicit()))
                .toList();
        if (!explicitProfiles.isEmpty()) {
            userInterestProfileRepository.deleteAll(explicitProfiles);
        }

        LocalDateTime now = LocalDateTime.now();
        for (Integer categoryId : interestedDomainIds) {
            UserInterestProfile profile = userInterestProfileRepository.findByUserIdAndCategoryId(user.getId(), categoryId)
                    .orElseGet(UserInterestProfile::new);
            profile.setUser(user);
            profile.setCategory(categoryRepository.getReferenceById(categoryId));
            profile.setInterestScore(BigDecimal.ONE);
            profile.setInteractionCount(profile.getInteractionCount() == null ? 0 : profile.getInteractionCount());
            profile.setTimeSpentMinutes(profile.getTimeSpentMinutes() == null ? 0 : profile.getTimeSpentMinutes());
            profile.setLastInteractionAt(now);
            profile.setLastUpdated(now);
            profile.setIsExplicit(true);
            userInterestProfileRepository.save(profile);
        }
    }

    private void syncPreferredSkills(UUID userId, List<Integer> preferredSkillIds) {
        userSkillRepository.deleteByUserId(userId);
        for (Integer skillId : preferredSkillIds) {
            userSkillRepository.save(UserSkill.builder()
                    .userId(userId)
                    .skillId(skillId)
                    .level("INTERMEDIATE")
                    .build());
        }
    }

    private List<Integer> dedupeIntegers(List<Integer> values) {
        if (values == null) return new ArrayList<>();
        Set<Integer> deduped = new LinkedHashSet<>();
        for (Integer value : values) {
            if (value != null) deduped.add(value);
        }
        return new ArrayList<>(deduped);
    }

    private List<String> normalizeTextList(List<String> values) {
        if (values == null) return new ArrayList<>();
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            if (value == null) continue;
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }
        return normalized.stream().distinct().limit(30).collect(Collectors.toList());
    }

    private List<String> defaultLanguage(User user) {
        if (user.getPreferredLanguage() == null) {
            return new ArrayList<>();
        }
        return List.of(user.getPreferredLanguage().name());
    }
}
