package com.mentorx.api.feature.feed.service.impl;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.feed.dto.response.CourseRecommendationResponse;
import com.mentorx.api.feature.feed.service.CourseRecommendationService;
import com.mentorx.api.feature.matching.repository.UserInterestProfileRepository;
import com.mentorx.api.feature.system.entity.Skill;
import com.mentorx.api.feature.system.entity.UserSkill;
import com.mentorx.api.feature.system.repository.CategoryRepository;
import com.mentorx.api.feature.system.repository.UserSkillRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
public class CourseRecommendationServiceImpl implements CourseRecommendationService {

    private static final BigDecimal MATCH_THRESHOLD = new BigDecimal("40.00");
    private static final int DEFAULT_LIMIT = 10;

    private final CourseRepository courseRepository;
    private final UserInterestProfileRepository userInterestProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public List<CourseRecommendationResponse> getRecommendedCourses(UUID userId, int limit) {
        Set<Integer> interestedCategoryIds = userInterestProfileRepository.findByUserIdOrderByInterestScoreDesc(userId).stream()
                .map(item -> item.getCategory().getId())
                .collect(Collectors.toSet());
        Set<String> preferredSkills = getUserSkills(userId);
        String preferredLanguage = getPrimaryLanguage(userId);

        Pageable pageable = PageRequest.of(0, 150);
        List<Course> courses = courseRepository.findByStatusAndDeletedAtIsNull(CourseStatus.PUBLISHED, pageable).getContent();

        List<CourseRecommendationResponse> personalized = courses.stream()
                .map(course -> calculateCourseMatchInternal(course, interestedCategoryIds, preferredSkills, preferredLanguage))
                .filter(Objects::nonNull)
                .filter(item -> item.getMatchScore().compareTo(MATCH_THRESHOLD) >= 0)
                .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()))
                .limit(limit)
                .collect(Collectors.toList());

        if (!personalized.isEmpty()) {
            return personalized;
        }

        // Fallback: popular/trending content when preferences are sparse.
        return courses.stream()
                .map(course -> calculateCourseMatchInternal(course, interestedCategoryIds, preferredSkills, preferredLanguage))
                .filter(Objects::nonNull)
                .sorted((a, b) -> {
                    int enrollCompare = Integer.compare(b.getTotalEnrollments(), a.getTotalEnrollments());
                    if (enrollCompare != 0) return enrollCompare;
                    return b.getAverageRating().compareTo(a.getAverageRating());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseRecommendationResponse> getRecommendedCourses(UUID userId) {
        return getRecommendedCourses(userId, DEFAULT_LIMIT);
    }

    @Override
    public CourseRecommendationResponse calculateCourseMatch(UUID userId, UUID courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null || course.getStatus() != CourseStatus.PUBLISHED || course.getDeletedAt() != null) {
            return null;
        }

        Set<Integer> interestedCategoryIds = userInterestProfileRepository.findByUserIdOrderByInterestScoreDesc(userId).stream()
                .map(item -> item.getCategory().getId())
                .collect(Collectors.toSet());
        Set<String> preferredSkills = getUserSkills(userId);
        String preferredLanguage = getPrimaryLanguage(userId);
        return calculateCourseMatchInternal(course, interestedCategoryIds, preferredSkills, preferredLanguage);
    }

    private CourseRecommendationResponse calculateCourseMatchInternal(
            Course course,
            Set<Integer> interestedCategoryIds,
            Set<String> preferredSkills,
            String preferredLanguage
    ) {
        BigDecimal score = BigDecimal.ZERO;

        if (course.getCategoryId() != null && interestedCategoryIds.contains(course.getCategoryId())) {
            score = score.add(new BigDecimal("40"));
        }

        Set<String> courseSkills = normalizeSkills(course.getSkills());
        score = score.add(calculateSkillScore(preferredSkills, courseSkills));

        if (course.getLanguage() != null && preferredLanguage != null
                && course.getLanguage().name().equalsIgnoreCase(preferredLanguage)) {
            score = score.add(new BigDecimal("10"));
        }

        score = score.add(calculatePopularityScore(course));
        score = score.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);

        String categoryName = null;
        if (course.getCategoryId() != null) {
            categoryName = categoryRepository.findById(course.getCategoryId())
                    .map(category -> category.getLabelEn() != null ? category.getLabelEn() : category.getLabelVi())
                    .orElse(null);
        }

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
                .language(course.getLanguage() != null ? course.getLanguage().name() : null)
                .skills(new ArrayList<>(courseSkills))
                .categoryId(course.getCategoryId())
                .categoryName(categoryName)
                .matchScore(score)
                .isCertificate(course.getIsCertificate())
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

    private String getPrimaryLanguage(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.getPreferredLanguage() != null ? user.getPreferredLanguage().name() : null)
                .orElse(null);
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

    private BigDecimal calculateSkillScore(Set<String> preferredSkills, Set<String> courseSkills) {
        if (preferredSkills.isEmpty() || courseSkills.isEmpty()) {
            return BigDecimal.ZERO;
        }
        long overlap = courseSkills.stream().filter(preferredSkills::contains).count();
        if (overlap <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(overlap * 10L).min(new BigDecimal("30"));
    }

    private BigDecimal calculatePopularityScore(Course course) {
        BigDecimal rating = course.getAverageRating() == null ? BigDecimal.ZERO : course.getAverageRating();
        BigDecimal ratingScore = rating
                .divide(new BigDecimal("5"), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("6"));
        BigDecimal enrollmentScore = BigDecimal.valueOf(Math.min(4, Math.max(0, course.getTotalEnrollments() / 20)));
        return ratingScore.add(enrollmentScore);
    }
}
