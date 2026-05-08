package com.mentorx.api.feature.feed.service.impl;

import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.feature.feed.dto.response.JobRecommendationResponse;
import com.mentorx.api.feature.feed.service.JobRecommendationService;
import com.mentorx.api.feature.feed.service.MatchingEngineService;
import com.mentorx.api.feature.job.entity.Job;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.system.entity.Category;
import com.mentorx.api.feature.system.repository.CategoryRepository;
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
 * Implementation of JobRecommendationService
 * Provides personalized job recommendations using the matching engine
 * Filters by user skills and budget range appropriate to skill level
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobRecommendationServiceImpl implements JobRecommendationService {

    private final JobRepository jobRepository;
    private final CategoryRepository categoryRepository;
    private final MatchingEngineService matchingEngineService;

    private static final BigDecimal MATCH_THRESHOLD = new BigDecimal("85.00");
    private static final int DEFAULT_LIMIT = 10;

    // Budget ranges by skill level (in MXC)
    private static final BigDecimal BEGINNER_MAX_BUDGET = new BigDecimal("500");
    private static final BigDecimal INTERMEDIATE_MIN_BUDGET = new BigDecimal("300");
    private static final BigDecimal INTERMEDIATE_MAX_BUDGET = new BigDecimal("1500");
    private static final BigDecimal ADVANCED_MIN_BUDGET = new BigDecimal("1000");

    @Override
    public List<JobRecommendationResponse> getRecommendedJobs(UUID userId, int limit) {
        log.info("Getting {} recommended jobs for user: {}", limit, userId);

        // Get user's skill level and interested categories
        String userLevel = matchingEngineService.getUserSkillLevel(userId);
        Set<String> userSkills = matchingEngineService.getUserSkills(userId);
        List<Integer> interestedCategories = matchingEngineService.getUserInterestedCategories(userId);

        if (userSkills.isEmpty() && interestedCategories.isEmpty()) {
            log.warn("User {} has no skills or interests, returning empty recommendations", userId);
            return Collections.emptyList();
        }

        log.debug("User {} level: {}, {} skills, {} categories", 
            userId, userLevel, userSkills.size(), interestedCategories.size());

        // Get open jobs (we'll filter and score them)
        Pageable pageable = PageRequest.of(0, 100); // Get top 100 jobs to score
        List<Job> jobs = jobRepository.findOpen(pageable).getContent();

        log.debug("Found {} open jobs to score", jobs.size());

        // Calculate match scores and filter
        List<JobRecommendationResponse> recommendations = jobs.stream()
            .map(job -> calculateJobMatchInternal(userId, job, userLevel, userSkills, interestedCategories))
            .filter(Objects::nonNull)
            .filter(rec -> rec.getMatchScore().compareTo(MATCH_THRESHOLD) >= 0)
            .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()))
            .limit(limit)
            .collect(Collectors.toList());

        log.info("Returning {} job recommendations for user: {}", recommendations.size(), userId);
        return recommendations;
    }

    @Override
    public List<JobRecommendationResponse> getRecommendedJobs(UUID userId) {
        return getRecommendedJobs(userId, DEFAULT_LIMIT);
    }

    @Override
    public JobRecommendationResponse calculateJobMatch(UUID userId, UUID jobId) {
        log.debug("Calculating match score for user {} and job {}", userId, jobId);

        Job job = jobRepository.findById(jobId)
            .orElse(null);

        if (job == null || job.getStatus() != JobStatus.OPEN || job.getDeletedAt() != null) {
            log.warn("Job {} not found or not open", jobId);
            return null;
        }

        String userLevel = matchingEngineService.getUserSkillLevel(userId);
        Set<String> userSkills = matchingEngineService.getUserSkills(userId);
        List<Integer> interestedCategories = matchingEngineService.getUserInterestedCategories(userId);

        return calculateJobMatchInternal(userId, job, userLevel, userSkills, interestedCategories);
    }

    /**
     * Internal method to calculate match score for a job
     * Implements budget range filtering based on skill level
     */
    private JobRecommendationResponse calculateJobMatchInternal(
            UUID userId,
            Job job,
            String userLevel,
            Set<String> userSkills,
            List<Integer> interestedCategories) {

        // Filter 1: Check category match (if job has category)
        if (job.getCategoryId() != null && !interestedCategories.isEmpty()) {
            if (!interestedCategories.contains(job.getCategoryId())) {
                log.debug("Job {} category {} not in user's interested categories", 
                    job.getId(), job.getCategoryId());
                return null;
            }
        }

        // Filter 2: Check budget range appropriate to skill level
        // Requirement 5.4: Budget ranges should match user's skill level
        if (!isBudgetAppropriate(job, userLevel)) {
            log.debug("Job {} budget not appropriate for user level {}", job.getId(), userLevel);
            return null;
        }

        // For jobs, we don't have explicit required skills in current schema
        // So we use empty set and rely on category matching
        Set<String> jobSkills = Collections.emptySet();

        // Calculate match score
        // Note: Without explicit job skills, the score will be based on category match and rating
        // In a real implementation, you'd want to add a job_skills table
        BigDecimal matchScore = matchingEngineService.calculateMatchScore(
            userId,
            jobSkills,
            userLevel,
            BigDecimal.ZERO, // Jobs don't have ratings
            job.getCategoryId()
        );

        // Boost score if user has skills (even though we can't match them explicitly)
        // This is a placeholder until job_skills table is added
        if (!userSkills.isEmpty()) {
            matchScore = matchScore.add(new BigDecimal("10")); // Add 10 points for having skills
        }

        // Cap at 100
        if (matchScore.compareTo(new BigDecimal("100")) > 0) {
            matchScore = new BigDecimal("100.00");
        }

        // Get category name
        String categoryName = null;
        if (job.getCategoryId() != null) {
            categoryName = categoryRepository.findById(job.getCategoryId())
                .map(Category::getLabelEn)
                .orElse(null);
        }

        // Build response
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
            .requiredSkills(Collections.emptyList()) // TODO: Add when job_skills table exists
            .proposalCount(job.getProposalCount())
            .publishedAt(job.getPublishedAt())
            .matchScore(matchScore)
            .isFeatured(job.getIsFeatured())
            .build();
    }

    /**
     * Check if job budget is appropriate for user's skill level
     * Requirement 5.4: Budget range filtering by skill level
     */
    private boolean isBudgetAppropriate(Job job, String userLevel) {
        BigDecimal budget = job.getBudgetMaxMxc() != null ? job.getBudgetMaxMxc() : job.getBudgetMinMxc();
        
        if (budget == null) {
            // No budget specified, allow it
            return true;
        }

        String normalizedLevel = userLevel.toUpperCase();

        switch (normalizedLevel) {
            case "BEGINNER":
                // Beginners: lower budgets (up to 500 MXC)
                return budget.compareTo(BEGINNER_MAX_BUDGET) <= 0;
                
            case "INTERMEDIATE":
                // Intermediate: medium budgets (300-1500 MXC)
                return budget.compareTo(INTERMEDIATE_MIN_BUDGET) >= 0 
                    && budget.compareTo(INTERMEDIATE_MAX_BUDGET) <= 0;
                
            case "ADVANCED":
                // Advanced: higher budgets (1000+ MXC)
                return budget.compareTo(ADVANCED_MIN_BUDGET) >= 0;
                
            default:
                // Unknown level, allow all budgets
                return true;
        }
    }
}
