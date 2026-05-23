package com.mentorx.api.feature.feed.service.impl;

import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.feature.feed.dto.response.JobRecommendationResponse;
import com.mentorx.api.feature.feed.service.JobRecommendationService;
import com.mentorx.api.feature.job.entity.Job;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.matching.repository.UserInterestProfileRepository;
import com.mentorx.api.feature.system.entity.Skill;
import com.mentorx.api.feature.system.entity.UserSkill;
import com.mentorx.api.feature.system.repository.CategoryRepository;
import com.mentorx.api.feature.system.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobRecommendationServiceImpl implements JobRecommendationService {

    private static final BigDecimal MATCH_THRESHOLD = new BigDecimal("40.00");
    private static final int DEFAULT_LIMIT = 10;

    private final JobRepository jobRepository;
    private final UserInterestProfileRepository userInterestProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<JobRecommendationResponse> getRecommendedJobs(UUID userId, int limit) {
        Set<Integer> interestedCategoryIds = userInterestProfileRepository.findByUserIdOrderByInterestScoreDesc(userId).stream()
                .map(item -> item.getCategory().getId())
                .collect(Collectors.toSet());
        Set<String> preferredSkills = getUserSkills(userId);

        Pageable pageable = PageRequest.of(0, 160);
        List<Job> jobs = jobRepository.findOpen(pageable).getContent();

        List<JobRecommendationResponse> personalized = jobs.stream()
                .map(job -> calculateJobMatchInternal(job, interestedCategoryIds, preferredSkills))
                .filter(Objects::nonNull)
                .filter(item -> item.getMatchScore().compareTo(MATCH_THRESHOLD) >= 0)
                .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()))
                .limit(limit)
                .collect(Collectors.toList());

        if (!personalized.isEmpty()) {
            return personalized;
        }

        // Fallback when there is no strong match yet.
        return jobs.stream()
                .map(job -> calculateJobMatchInternal(job, interestedCategoryIds, preferredSkills))
                .filter(Objects::nonNull)
                .sorted((a, b) -> {
                    LocalDateTime aTime = a.getPublishedAt() != null ? a.getPublishedAt() : LocalDateTime.MIN;
                    LocalDateTime bTime = b.getPublishedAt() != null ? b.getPublishedAt() : LocalDateTime.MIN;
                    return bTime.compareTo(aTime);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobRecommendationResponse> getRecommendedJobs(UUID userId) {
        return getRecommendedJobs(userId, DEFAULT_LIMIT);
    }

    @Override
    public JobRecommendationResponse calculateJobMatch(UUID userId, UUID jobId) {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null || job.getStatus() != JobStatus.OPEN || job.getDeletedAt() != null) {
            return null;
        }
        Set<Integer> interestedCategoryIds = userInterestProfileRepository.findByUserIdOrderByInterestScoreDesc(userId).stream()
                .map(item -> item.getCategory().getId())
                .collect(Collectors.toSet());
        Set<String> preferredSkills = getUserSkills(userId);
        return calculateJobMatchInternal(job, interestedCategoryIds, preferredSkills);
    }

    private JobRecommendationResponse calculateJobMatchInternal(
            Job job,
            Set<Integer> interestedCategoryIds,
            Set<String> preferredSkills
    ) {
        BigDecimal score = BigDecimal.ZERO;

        if (job.getCategoryId() != null && interestedCategoryIds.contains(job.getCategoryId())) {
            score = score.add(new BigDecimal("45"));
        }

        Set<String> requiredSkills = normalizeSkills(job.getRequiredSkills());
        score = score.add(calculateSkillScore(preferredSkills, requiredSkills));

        if (job.getPreferredLanguage() != null) {
            // Minimal language signal (+10) when job has explicit language and user provided any skill profile.
            if (!preferredSkills.isEmpty()) {
                score = score.add(new BigDecimal("10"));
            }
        }

        score = score.add(calculateFreshnessScore(job.getPublishedAt()));
        score = score.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);

        String categoryName = null;
        if (job.getCategoryId() != null) {
            categoryName = categoryRepository.findById(job.getCategoryId())
                    .map(category -> category.getLabelEn() != null ? category.getLabelEn() : category.getLabelVi())
                    .orElse(null);
        }

        return JobRecommendationResponse.builder()
                .jobId(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .jobType(job.getJobType())
                .budgetType(job.getBudgetType())
                .budgetMin(job.getBudgetMinMxc())
                .budgetMax(job.getBudgetMaxMxc())
                .hourlyRate(job.getHourlyRateMxc())
                .estimatedHours(job.getEstimatedHours())
                .deadlineAt(job.getDeadlineAt())
                .clientName(job.getClient().getFullName())
                .clientId(job.getClient().getId())
                .categoryId(job.getCategoryId())
                .categoryName(categoryName)
                .requiredSkills(new ArrayList<>(requiredSkills))
                .proposalCount(job.getProposalCount())
                .publishedAt(job.getPublishedAt())
                .matchScore(score)
                .isFeatured(job.getIsFeatured())
                .build();
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
        if (skills == null) return new HashSet<>();
        return skills.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private BigDecimal calculateSkillScore(Set<String> preferredSkills, Set<String> requiredSkills) {
        if (preferredSkills.isEmpty() || requiredSkills.isEmpty()) {
            return BigDecimal.ZERO;
        }
        long overlap = requiredSkills.stream().filter(preferredSkills::contains).count();
        if (overlap <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(overlap * 12L).min(new BigDecimal("35"));
    }

    private BigDecimal calculateFreshnessScore(LocalDateTime publishedAt) {
        if (publishedAt == null) {
            return new BigDecimal("2");
        }
        long hours = Duration.between(publishedAt, LocalDateTime.now()).toHours();
        if (hours <= 24) {
            return new BigDecimal("5");
        }
        if (hours <= 72) {
            return new BigDecimal("3");
        }
        return new BigDecimal("1");
    }
}

