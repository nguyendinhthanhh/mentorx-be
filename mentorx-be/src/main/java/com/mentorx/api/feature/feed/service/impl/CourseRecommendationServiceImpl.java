package com.mentorx.api.feature.feed.service.impl;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.feed.dto.response.CourseRecommendationResponse;
import com.mentorx.api.feature.feed.service.CourseRecommendationService;
import com.mentorx.api.feature.feed.service.MatchingEngineService;
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
 * Implementation of CourseRecommendationService
 * Provides personalized course recommendations using the matching engine
 * Filters by BOTH skill level AND interest categories (multi-criteria filtering)
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseRecommendationServiceImpl implements CourseRecommendationService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final MatchingEngineService matchingEngineService;

    private static final BigDecimal MATCH_THRESHOLD = new BigDecimal("85.00");
    private static final int DEFAULT_LIMIT = 10;

    @Override
    public List<CourseRecommendationResponse> getRecommendedCourses(UUID userId, int limit) {
        log.info("Getting {} recommended courses for user: {}", limit, userId);

        // Get user's skill level and interested categories
        String userLevel = matchingEngineService.getUserSkillLevel(userId);
        List<Integer> interestedCategories = matchingEngineService.getUserInterestedCategories(userId);

        if (interestedCategories.isEmpty()) {
            log.warn("User {} has no interest profiles, returning empty recommendations", userId);
            return Collections.emptyList();
        }

        log.debug("User {} level: {}, interested in {} categories", userId, userLevel, interestedCategories.size());

        // Get published courses (we'll filter and score them)
        Pageable pageable = PageRequest.of(0, 100); // Get top 100 courses to score
        List<Course> courses = courseRepository
            .findByStatusAndDeletedAtIsNull(CourseStatus.PUBLISHED, pageable)
            .getContent();

        log.debug("Found {} published courses to score", courses.size());

        // Calculate match scores and filter
        List<CourseRecommendationResponse> recommendations = courses.stream()
            .map(course -> calculateCourseMatchInternal(userId, course, userLevel, interestedCategories))
            .filter(Objects::nonNull)
            .filter(rec -> rec.getMatchScore().compareTo(MATCH_THRESHOLD) >= 0)
            .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()))
            .limit(limit)
            .collect(Collectors.toList());

        log.info("Returning {} course recommendations for user: {}", recommendations.size(), userId);
        return recommendations;
    }

    @Override
    public List<CourseRecommendationResponse> getRecommendedCourses(UUID userId) {
        return getRecommendedCourses(userId, DEFAULT_LIMIT);
    }

    @Override
    public CourseRecommendationResponse calculateCourseMatch(UUID userId, UUID courseId) {
        log.debug("Calculating match score for user {} and course {}", userId, courseId);

        Course course = courseRepository.findById(courseId)
            .orElse(null);

        if (course == null || course.getStatus() != CourseStatus.PUBLISHED || course.getDeletedAt() != null) {
            log.warn("Course {} not found or not published", courseId);
            return null;
        }

        String userLevel = matchingEngineService.getUserSkillLevel(userId);
        List<Integer> interestedCategories = matchingEngineService.getUserInterestedCategories(userId);

        return calculateCourseMatchInternal(userId, course, userLevel, interestedCategories);
    }

    /**
     * Internal method to calculate match score for a course
     * Implements multi-criteria filtering: skill level AND category
     */
    private CourseRecommendationResponse calculateCourseMatchInternal(
            UUID userId,
            Course course,
            String userLevel,
            List<Integer> interestedCategories) {

        // Multi-criteria filtering: Check BOTH level AND category
        // Requirement 4.2: Courses must match both skill level AND interest categories

        // Filter 1: Check skill level match
        if (course.getLevel() == null || !course.getLevel().equalsIgnoreCase(userLevel)) {
            log.debug("Course {} level {} doesn't match user level {}", 
                course.getId(), course.getLevel(), userLevel);
            return null;
        }

        // Filter 2: Check category match
        if (course.getCategoryId() == null || !interestedCategories.contains(course.getCategoryId())) {
            log.debug("Course {} category {} not in user's interested categories", 
                course.getId(), course.getCategoryId());
            return null;
        }

        // Both filters passed - calculate match score
        // For courses, we don't have explicit skills, so we use empty set
        // The score will be based primarily on level match and rating
        Set<String> courseSkills = Collections.emptySet();

        BigDecimal matchScore = matchingEngineService.calculateMatchScore(
            userId,
            courseSkills,
            course.getLevel(),
            course.getAverageRating(),
            course.getCategoryId()
        );

        // Get category name
        String categoryName = null;
        if (course.getCategoryId() != null) {
            categoryName = categoryRepository.findById(course.getCategoryId())
                .map(Category::getLabelEn)
                .orElse(null);
        }

        // Build response
        return CourseRecommendationResponse.builder()
            .courseId(course.getId())
            .title(course.getTitle())
            .slug(course.getSlug())
            .description(course.getDescription())
            .thumbnailUrl(course.getThumbnailUrl())
            .price(course.getPriceMxc())
            .instructorName(course.getInstructor().getFullName())
            .instructorId(course.getInstructor().getId())
            .averageRating(course.getAverageRating())
            .totalReviews(course.getTotalReviews())
            .totalEnrollments(course.getTotalEnrollments())
            .totalDurationMinutes(course.getTotalDurationMin())
            .totalLessons(course.getTotalLessons() != null ? course.getTotalLessons().intValue() : 0)
            .level(course.getLevel())
            .language(course.getLanguage().name())
            .skills(Collections.emptyList()) // Courses don't have explicit skills in current schema
            .categoryId(course.getCategoryId())
            .categoryName(categoryName)
            .matchScore(matchScore)
            .isCertificate(course.getIsCertificate())
            .build();
    }
}
