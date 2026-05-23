package com.mentorx.api.feature.feed.service.impl;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.feature.feed.dto.response.MentorRecommendationResponse;
import com.mentorx.api.feature.feed.service.MentorRecommendationService;
import com.mentorx.api.feature.matching.repository.UserInterestProfileRepository;
import com.mentorx.api.feature.system.entity.Category;
import com.mentorx.api.feature.system.entity.Skill;
import com.mentorx.api.feature.system.entity.UserSkill;
import com.mentorx.api.feature.system.repository.CategoryRepository;
import com.mentorx.api.feature.system.repository.UserSkillRepository;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentorRecommendationServiceImpl implements MentorRecommendationService {

    private static final BigDecimal MATCH_THRESHOLD = new BigDecimal("40.00");
    private static final int DEFAULT_LIMIT = 10;

    private final MentorProfileRepository mentorProfileRepository;
    private final UserInterestProfileRepository userInterestProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<MentorRecommendationResponse> getRecommendedMentors(UUID userId, int limit) {
        log.info("Getting {} recommended mentors for user {}", limit, userId);

        Set<String> interestedDomains = getInterestedDomainNames(userId);
        Set<String> userSkills = getUserSkills(userId);

        Pageable pageable = PageRequest.of(0, 120);
        List<MentorProfile> mentors = mentorProfileRepository.findApproved(pageable).getContent();

        List<MentorRecommendationResponse> personalized = mentors.stream()
                .map(mentor -> calculateMentorMatchInternal(mentor, interestedDomains, userSkills))
                .filter(Objects::nonNull)
                .filter(item -> item.getMatchScore().compareTo(MATCH_THRESHOLD) >= 0)
                .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()))
                .limit(limit)
                .collect(Collectors.toList());

        if (!personalized.isEmpty()) {
            return personalized;
        }

        // Fallback: still show quality mentors (popular/top-rated) when there is no strong personal match yet.
        return mentors.stream()
                .map(mentor -> calculateMentorMatchInternal(mentor, interestedDomains, userSkills))
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getAverageRating().compareTo(a.getAverageRating()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<MentorRecommendationResponse> getRecommendedMentors(UUID userId) {
        return getRecommendedMentors(userId, DEFAULT_LIMIT);
    }

    @Override
    public MentorRecommendationResponse calculateMentorMatch(UUID userId, UUID mentorId) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId).orElse(null);
        if (mentor == null || mentor.getUser().getMentorStatus() != MentorStatus.APPROVED) {
            return null;
        }
        return calculateMentorMatchInternal(mentor, getInterestedDomainNames(userId), getUserSkills(userId));
    }

    private MentorRecommendationResponse calculateMentorMatchInternal(
            MentorProfile mentor,
            Set<String> interestedDomains,
            Set<String> userSkills
    ) {
        User mentorUser = mentor.getUser();
        if (mentorUser == null || mentorUser.getMentorStatus() != MentorStatus.APPROVED) {
            return null;
        }

        Set<String> mentorSkills = normalizeSkills(mentor.getSkills());
        if (mentorSkills.isEmpty()) {
            mentorSkills = userSkillRepository.findByUserId(mentorUser.getId()).stream()
                    .map(UserSkill::getSkill)
                    .map(Skill::getLabelEn)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        }

        BigDecimal score = BigDecimal.ZERO;

        String primaryDomain = mentor.getPrimaryDomain() == null ? "" : mentor.getPrimaryDomain().trim().toLowerCase();
        if (!primaryDomain.isBlank() && interestedDomains.stream().anyMatch(domain -> domain.contains(primaryDomain) || primaryDomain.contains(domain))) {
            score = score.add(new BigDecimal("40"));
        }

        score = score.add(calculateSkillScore(userSkills, mentorSkills));

        if (mentor.getLanguages() != null && mentorUser.getPreferredLanguage() != null) {
            boolean languageMatch = mentor.getLanguages().stream()
                    .filter(Objects::nonNull)
                    .map(value -> value.trim().toLowerCase())
                    .anyMatch(value -> value.equals(mentorUser.getPreferredLanguage().name().toLowerCase()));
            if (languageMatch) {
                score = score.add(new BigDecimal("10"));
            }
        }

        BigDecimal rating = mentor.getAverageRating() == null ? BigDecimal.ZERO : mentor.getAverageRating();
        BigDecimal ratingScore = rating
                .divide(new BigDecimal("5"), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("10"))
                .min(new BigDecimal("10"));
        score = score.add(ratingScore);

        if ("available".equalsIgnoreCase(mentor.getAvailability())) {
            score = score.add(new BigDecimal("10"));
        }

        score = score.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);

        return MentorRecommendationResponse.builder()
                .mentorId(mentor.getId())
                .userId(mentorUser.getId())
                .fullName(mentorUser.getFullName())
                .displayName(mentorUser.getDisplayName())
                .avatarUrl(mentorUser.getAvatarUrl())
                .headline(mentor.getHeadline())
                .hourlyRate(mentor.getHourlyRateMxc())
                .averageRating(rating)
                .totalReviews(mentor.getTotalReviews())
                .totalJobsDone(mentor.getTotalJobsDone())
                .successRate(mentor.getSuccessRate())
                .availability(mentor.getAvailability())
                .responseTimeHours(mentor.getResponseTimeHours() != null ? mentor.getResponseTimeHours().intValue() : null)
                .skills(new ArrayList<>(mentorSkills))
                .categories(primaryDomain.isBlank() ? Collections.emptyList() : List.of(mentor.getPrimaryDomain()))
                .matchScore(score)
                .isFeatured(mentor.getIsFeatured())
                .isAvailable("available".equalsIgnoreCase(mentor.getAvailability()))
                .build();
    }

    private Set<String> getInterestedDomainNames(UUID userId) {
        List<Integer> interestedIds = userInterestProfileRepository.findByUserIdOrderByInterestScoreDesc(userId).stream()
                .map(item -> item.getCategory().getId())
                .distinct()
                .toList();
        if (interestedIds.isEmpty()) {
            return new HashSet<>();
        }

        return categoryRepository.findAllById(interestedIds).stream()
                .flatMap(category -> Stream.of(category.getLabelEn(), category.getLabelVi(), category.getSlug()))
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private Set<String> getUserSkills(UUID userId) {
        return userSkillRepository.findByUserId(userId).stream()
                .map(UserSkill::getSkill)
                .map(Skill::getLabelEn)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private Set<String> normalizeSkills(List<String> skills) {
        if (skills == null) {
            return new HashSet<>();
        }
        return skills.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private BigDecimal calculateSkillScore(Set<String> userSkills, Set<String> mentorSkills) {
        if (userSkills.isEmpty() || mentorSkills.isEmpty()) {
            return BigDecimal.ZERO;
        }
        long overlap = mentorSkills.stream().filter(userSkills::contains).count();
        if (overlap <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(overlap * 12L).min(new BigDecimal("30"));
    }
}
