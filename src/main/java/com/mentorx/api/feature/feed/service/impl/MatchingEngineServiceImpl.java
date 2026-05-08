package com.mentorx.api.feature.feed.service.impl;

import com.mentorx.api.feature.feed.service.MatchingEngineService;
import com.mentorx.api.feature.matching.entity.UserInterestProfile;
import com.mentorx.api.feature.matching.repository.UserInterestProfileRepository;
import com.mentorx.api.feature.system.entity.UserSkill;
import com.mentorx.api.feature.system.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of MatchingEngineService
 * Calculates match scores using the formula:
 * matchScore = (skillMatch * 0.6) + (levelMatch * 0.3) + (ratingBonus * 0.1)
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingEngineServiceImpl implements MatchingEngineService {

    private final UserInterestProfileRepository userInterestProfileRepository;
    private final UserSkillRepository userSkillRepository;

    // Weights for match score calculation
    private static final BigDecimal SKILL_WEIGHT = new BigDecimal("0.6");
    private static final BigDecimal LEVEL_WEIGHT = new BigDecimal("0.3");
    private static final BigDecimal RATING_WEIGHT = new BigDecimal("0.1");

    // Points awarded for matches
    private static final BigDecimal POINTS_PER_SKILL = new BigDecimal("20");
    private static final BigDecimal POINTS_FOR_LEVEL_MATCH = new BigDecimal("15");
    private static final BigDecimal MAX_RATING_BONUS = new BigDecimal("10");

    // Default threshold for recommendations
    private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal("85.00");

    @Override
    public BigDecimal calculateMatchScore(
            UUID userId,
            Set<String> itemSkills,
            String itemLevel,
            BigDecimal itemRating,
            Integer itemCategoryId) {
        
        log.debug("Calculating match score for user {} with item in category {}", userId, itemCategoryId);

        // Check category interest first
        if (itemCategoryId != null && !isInterestedInCategory(userId, itemCategoryId)) {
            log.debug("User {} not interested in category {}, returning 0 score", userId, itemCategoryId);
            return BigDecimal.ZERO;
        }

        // Get user data
        Set<String> userSkills = getUserSkills(userId);
        String userLevel = getUserSkillLevel(userId);

        // Calculate components
        BigDecimal skillMatch = calculateSkillMatch(userSkills, itemSkills);
        BigDecimal levelMatch = calculateLevelMatch(userLevel, itemLevel);
        BigDecimal ratingBonus = calculateRatingBonus(itemRating);

        // Apply weights and calculate final score
        BigDecimal weightedSkillMatch = skillMatch.multiply(SKILL_WEIGHT);
        BigDecimal weightedLevelMatch = levelMatch.multiply(LEVEL_WEIGHT);
        BigDecimal weightedRatingBonus = ratingBonus.multiply(RATING_WEIGHT);

        BigDecimal totalScore = weightedSkillMatch
            .add(weightedLevelMatch)
            .add(weightedRatingBonus)
            .setScale(2, RoundingMode.HALF_UP);

        // Cap at 100.00
        if (totalScore.compareTo(new BigDecimal("100.00")) > 0) {
            totalScore = new BigDecimal("100.00");
        }

        log.debug("Match score for user {}: {} (skill: {}, level: {}, rating: {})",
            userId, totalScore, skillMatch, levelMatch, ratingBonus);

        return totalScore;
    }

    @Override
    public BigDecimal calculateSkillMatch(Set<String> userSkills, Set<String> itemSkills) {
        if (userSkills == null || userSkills.isEmpty() || 
            itemSkills == null || itemSkills.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Normalize skill names to lowercase for comparison
        Set<String> normalizedUserSkills = userSkills.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        
        Set<String> normalizedItemSkills = itemSkills.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        // Count matching skills
        long matchingSkills = normalizedItemSkills.stream()
            .filter(normalizedUserSkills::contains)
            .count();

        // Award 20 points per matching skill
        BigDecimal skillScore = POINTS_PER_SKILL.multiply(BigDecimal.valueOf(matchingSkills));

        log.debug("Skill match: {} matching skills = {} points", matchingSkills, skillScore);
        return skillScore;
    }

    @Override
    public BigDecimal calculateLevelMatch(String userLevel, String itemLevel) {
        if (userLevel == null || itemLevel == null) {
            return BigDecimal.ZERO;
        }

        // Normalize levels to uppercase for comparison
        String normalizedUserLevel = userLevel.trim().toUpperCase();
        String normalizedItemLevel = itemLevel.trim().toUpperCase();

        // Award 15 points if levels match
        if (normalizedUserLevel.equals(normalizedItemLevel)) {
            log.debug("Level match: {} = {} points", normalizedUserLevel, POINTS_FOR_LEVEL_MATCH);
            return POINTS_FOR_LEVEL_MATCH;
        }

        log.debug("Level mismatch: user={}, item={} = 0 points", normalizedUserLevel, normalizedItemLevel);
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateRatingBonus(BigDecimal rating) {
        if (rating == null || rating.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Formula: (rating / 5) * 10
        BigDecimal bonus = rating
            .divide(new BigDecimal("5"), 4, RoundingMode.HALF_UP)
            .multiply(MAX_RATING_BONUS)
            .setScale(2, RoundingMode.HALF_UP);

        // Cap at 10.00
        if (bonus.compareTo(MAX_RATING_BONUS) > 0) {
            bonus = MAX_RATING_BONUS;
        }

        log.debug("Rating bonus: {} rating = {} points", rating, bonus);
        return bonus;
    }

    @Override
    public boolean isInterestedInCategory(UUID userId, Integer categoryId) {
        return userInterestProfileRepository
            .findByUserIdAndCategoryId(userId, categoryId)
            .isPresent();
    }

    @Override
    public String getUserSkillLevel(UUID userId) {
        List<UserInterestProfile> profiles = userInterestProfileRepository
            .findByUserIdOrderByInterestScoreDesc(userId);

        if (profiles.isEmpty()) {
            log.debug("No interest profiles found for user {}, defaulting to INTERMEDIATE", userId);
            return "INTERMEDIATE";
        }

        // Get the most common level from user's skills
        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
        
        if (userSkills.isEmpty()) {
            log.debug("No skills found for user {}, defaulting to INTERMEDIATE", userId);
            return "INTERMEDIATE";
        }

        // Count level occurrences
        Map<String, Long> levelCounts = userSkills.stream()
            .filter(skill -> skill.getLevel() != null)
            .collect(Collectors.groupingBy(
                skill -> skill.getLevel().toUpperCase(),
                Collectors.counting()
            ));

        // Return most common level or INTERMEDIATE as default
        String mostCommonLevel = levelCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("INTERMEDIATE");

        log.debug("User {} skill level: {}", userId, mostCommonLevel);
        return mostCommonLevel;
    }

    @Override
    public Set<String> getUserSkills(UUID userId) {
        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
        
        Set<String> skills = userSkills.stream()
            .map(userSkill -> userSkill.getSkill().getLabelEn())
            .collect(Collectors.toSet());

        log.debug("User {} has {} skills", userId, skills.size());
        return skills;
    }

    @Override
    public List<Integer> getUserInterestedCategories(UUID userId) {
        List<UserInterestProfile> profiles = userInterestProfileRepository
            .findByUserIdOrderByInterestScoreDesc(userId);

        List<Integer> categoryIds = profiles.stream()
            .map(profile -> profile.getCategory().getId())
            .collect(Collectors.toList());

        log.debug("User {} interested in {} categories", userId, categoryIds.size());
        return categoryIds;
    }

    @Override
    public <T> List<T> filterByThreshold(List<T> items, BigDecimal threshold) {
        // This is a generic method - actual filtering would need to be done
        // in the specific recommendation services where we know the item type
        // and can access the match score field
        log.debug("Filtering items by threshold: {}", threshold);
        return items;
    }

    @Override
    public <T> List<T> sortByMatchScore(List<T> items) {
        // This is a generic method - actual sorting would need to be done
        // in the specific recommendation services where we know the item type
        // and can access the match score field
        log.debug("Sorting items by match score");
        return items;
    }
}
