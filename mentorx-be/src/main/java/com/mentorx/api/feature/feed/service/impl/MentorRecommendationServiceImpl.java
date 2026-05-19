package com.mentorx.api.feature.feed.service.impl;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.feature.feed.dto.response.MentorRecommendationResponse;
import com.mentorx.api.feature.feed.service.MatchingEngineService;
import com.mentorx.api.feature.feed.service.MentorRecommendationService;
import com.mentorx.api.feature.system.entity.Category;
import com.mentorx.api.feature.system.entity.UserSkill;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of MentorRecommendationService
 * Provides personalized mentor recommendations using the matching engine
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentorRecommendationServiceImpl implements MentorRecommendationService {

    private final MentorProfileRepository mentorProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final MatchingEngineService matchingEngineService;

    private static final BigDecimal MATCH_THRESHOLD = new BigDecimal("85.00");
    private static final int DEFAULT_LIMIT = 10;

    @Override
    public List<MentorRecommendationResponse> getRecommendedMentors(UUID userId, int limit) {
        log.info("Getting {} recommended mentors for user: {}", limit, userId);

        // Get user's interested categories
        List<Integer> interestedCategories = matchingEngineService.getUserInterestedCategories(userId);
        
        if (interestedCategories.isEmpty()) {
            log.warn("User {} has no interest profiles, returning empty recommendations", userId);
            return Collections.emptyList();
        }

        // Get all approved mentors (we'll filter and score them)
        Pageable pageable = PageRequest.of(0, 100); // Get top 100 mentors to score
        List<MentorProfile> mentors = mentorProfileRepository
            .findApproved(pageable)
            .getContent();

        log.debug("Found {} approved mentors to score", mentors.size());

        // Calculate match scores and filter
        List<MentorRecommendationResponse> recommendations = mentors.stream()
            .map(mentor -> calculateMentorMatchInternal(userId, mentor, interestedCategories))
            .filter(Objects::nonNull)
            .filter(rec -> rec.getMatchScore().compareTo(MATCH_THRESHOLD) >= 0)
            .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()))
            .limit(limit)
            .collect(Collectors.toList());

        log.info("Returning {} mentor recommendations for user: {}", recommendations.size(), userId);
        return recommendations;
    }

    @Override
    public List<MentorRecommendationResponse> getRecommendedMentors(UUID userId) {
        return getRecommendedMentors(userId, DEFAULT_LIMIT);
    }

    @Override
    public MentorRecommendationResponse calculateMentorMatch(UUID userId, UUID mentorId) {
        log.debug("Calculating match score for user {} and mentor {}", userId, mentorId);

        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
            .orElse(null);

        if (mentor == null || mentor.getUser().getMentorStatus() != MentorStatus.APPROVED) {
            log.warn("Mentor {} not found or not approved", mentorId);
            return null;
        }

        List<Integer> interestedCategories = matchingEngineService.getUserInterestedCategories(userId);
        return calculateMentorMatchInternal(userId, mentor, interestedCategories);
    }

    /**
     * Internal method to calculate match score for a mentor
     */
    private MentorRecommendationResponse calculateMentorMatchInternal(
            UUID userId,
            MentorProfile mentor,
            List<Integer> interestedCategories) {

        User mentorUser = mentor.getUser();

        // Get mentor's skills
        List<UserSkill> mentorSkills = userSkillRepository.findByUserId(mentorUser.getId());
        Set<String> mentorSkillNames = mentorSkills.stream()
            .map(skill -> skill.getSkill().getLabelEn())
            .collect(Collectors.toSet());

        // Get mentor's categories (from skills)
        Set<Integer> mentorCategoryIds = mentorSkills.stream()
            .map(skill -> skill.getSkill().getId())
            .collect(Collectors.toSet());

        // Check if mentor has any matching category
        boolean hasMatchingCategory = mentorCategoryIds.stream()
            .anyMatch(interestedCategories::contains);

        if (!hasMatchingCategory && !interestedCategories.isEmpty()) {
            log.debug("Mentor {} has no matching categories with user {}", mentor.getId(), userId);
            return null;
        }

        // Get mentor's level (most common level from their skills)
        String mentorLevel = getMentorLevel(mentorSkills);

        // Get first matching category for scoring
        Integer categoryId = mentorCategoryIds.stream()
            .filter(interestedCategories::contains)
            .findFirst()
            .orElse(null);

        // Calculate match score
        BigDecimal matchScore = matchingEngineService.calculateMatchScore(
            userId,
            mentorSkillNames,
            mentorLevel,
            mentor.getAverageRating(),
            categoryId
        );

        // Get mentor's categories for response
        List<String> categoryNames = mentorSkills.stream()
            .map(skill -> skill.getSkill().getLabelEn())
            .distinct()
            .collect(Collectors.toList());

        // Build response
        return MentorRecommendationResponse.builder()
            .mentorId(mentor.getId())
            .userId(mentorUser.getId())
            .fullName(mentorUser.getFullName())
            .displayName(mentorUser.getDisplayName())
            .avatarUrl(mentorUser.getAvatarUrl())
            .headline(mentor.getHeadline())
            .hourlyRate(mentor.getHourlyRateMxc())
            .averageRating(mentor.getAverageRating())
            .totalReviews(mentor.getTotalReviews())
            .totalJobsDone(mentor.getTotalJobsDone())
            .successRate(mentor.getSuccessRate())
            .availability(mentor.getAvailability())
            .responseTimeHours(mentor.getResponseTimeHours() != null ? mentor.getResponseTimeHours().intValue() : null)
            .skills(new ArrayList<>(mentorSkillNames))
            .categories(categoryNames)
            .matchScore(matchScore)
            .isFeatured(mentor.getIsFeatured())
            .isAvailable("Available".equalsIgnoreCase(mentor.getAvailability()))
            .build();
    }

    /**
     * Get mentor's skill level (most common level from their skills)
     */
    private String getMentorLevel(List<UserSkill> mentorSkills) {
        if (mentorSkills.isEmpty()) {
            return "INTERMEDIATE";
        }

        Map<String, Long> levelCounts = mentorSkills.stream()
            .filter(skill -> skill.getLevel() != null)
            .collect(Collectors.groupingBy(
                skill -> skill.getLevel().toUpperCase(),
                Collectors.counting()
            ));

        return levelCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("INTERMEDIATE");
    }
}
