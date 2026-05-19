package com.mentorx.api.feature.feed.service.impl;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.enums.FeedItemType;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.feed.dto.response.CourseRecommendationResponse;
import com.mentorx.api.feature.feed.dto.response.JobRecommendationResponse;
import com.mentorx.api.feature.feed.dto.response.MentorRecommendationResponse;
import com.mentorx.api.feature.feed.dto.response.PersonalizedFeedResponse;
import com.mentorx.api.feature.feed.entity.PrecomputedFeedItem;
import com.mentorx.api.feature.feed.repository.PrecomputedFeedItemRepository;
import com.mentorx.api.feature.feed.service.*;
import com.mentorx.api.feature.job.entity.Job;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.system.entity.Category;
import com.mentorx.api.feature.system.entity.UserSkill;
import com.mentorx.api.feature.system.repository.CategoryRepository;
import com.mentorx.api.feature.system.repository.UserSkillRepository;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of FeedOrchestrationService
 * Orchestrates personalized feed generation with cache-first strategy
 * 
 * Strategy:
 * 1. Check Redis cache (< 100ms)
 * 2. Fallback to database precomputed items (< 200ms)
 * 3. Fallback to real-time computation (< 500ms)
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedOrchestrationServiceImpl implements FeedOrchestrationService {

    private final CacheService cacheService;
    private final PrecomputedFeedItemRepository precomputedFeedItemRepository;
    private final MentorRecommendationService mentorRecommendationService;
    private final CourseRecommendationService courseRecommendationService;
    private final JobRecommendationService jobRecommendationService;
    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final CourseRepository courseRepository;
    private final JobRepository jobRepository;
    private final CategoryRepository categoryRepository;
    private final UserSkillRepository userSkillRepository;

    private static final int DEFAULT_MENTOR_LIMIT = 10;
    private static final int DEFAULT_COURSE_LIMIT = 8;
    private static final int DEFAULT_JOB_LIMIT = 6;
    private static final BigDecimal MATCH_THRESHOLD = new BigDecimal("85.00");

    @Override
    public PersonalizedFeedResponse getPersonalizedFeed(UUID userId) {
        return getPersonalizedFeed(userId, DEFAULT_MENTOR_LIMIT, DEFAULT_COURSE_LIMIT, DEFAULT_JOB_LIMIT);
    }

    @Override
    public PersonalizedFeedResponse getPersonalizedFeed(UUID userId, int mentorLimit, int courseLimit, int jobLimit) {
        log.info("Getting personalized feed for user: {} (mentors: {}, courses: {}, jobs: {})", 
                 userId, mentorLimit, courseLimit, jobLimit);

        long startTime = System.currentTimeMillis();

        // Strategy 1: Check Redis cache
        PersonalizedFeedResponse cachedFeed = getFeedFromCache(userId);
        if (cachedFeed != null) {
            log.info("Feed retrieved from cache for user: {} in {}ms", 
                     userId, System.currentTimeMillis() - startTime);
            return limitFeedItems(cachedFeed, mentorLimit, courseLimit, jobLimit);
        }

        // Strategy 2: Fallback to database precomputed items
        PersonalizedFeedResponse dbFeed = getFeedFromDatabase(userId, mentorLimit, courseLimit, jobLimit);
        if (dbFeed != null) {
            log.info("Feed retrieved from database for user: {} in {}ms", 
                     userId, System.currentTimeMillis() - startTime);
            
            // Cache the database feed for future requests
            cacheFeed(userId, dbFeed);
            return dbFeed;
        }

        // Strategy 3: Fallback to real-time computation
        PersonalizedFeedResponse realTimeFeed = computeFeedRealTime(userId, mentorLimit, courseLimit, jobLimit);
        log.info("Feed computed in real-time for user: {} in {}ms", 
                 userId, System.currentTimeMillis() - startTime);

        // Cache the real-time feed
        cacheFeed(userId, realTimeFeed);

        // Store in database for future use (async would be better, but keeping it simple)
        storeFeedInDatabase(userId, realTimeFeed);

        return realTimeFeed;
    }

    @Override
    @Transactional
    public void precomputeFeedForUser(UUID userId) {
        log.info("Precomputing feed for user: {}", userId);

        try {
            // Delete existing precomputed items for this user
            precomputedFeedItemRepository.deleteByUserId(userId);

            // Compute fresh recommendations
            PersonalizedFeedResponse feed = computeFeedRealTime(userId, 
                DEFAULT_MENTOR_LIMIT, DEFAULT_COURSE_LIMIT, DEFAULT_JOB_LIMIT);

            // Store in database
            storeFeedInDatabase(userId, feed);

            // Invalidate cache to force fresh retrieval
            cacheService.invalidateUserFeed(userId);

            log.info("Successfully precomputed feed for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to precompute feed for user: {}", userId, e);
            throw e;
        }
    }

    @Override
    public void invalidateUserFeed(UUID userId) {
        log.info("Invalidating feed cache for user: {}", userId);
        cacheService.invalidateUserFeed(userId);
    }

    @Override
    public void invalidateAllFeeds() {
        log.info("Invalidating all feed caches");
        cacheService.invalidateAllFeeds();
    }

    /**
     * Get feed from Redis cache
     */
    private PersonalizedFeedResponse getFeedFromCache(UUID userId) {
        try {
            String cacheKey = cacheService.generateFeedKey(userId);
            Optional<PersonalizedFeedResponse> cached = cacheService.get(cacheKey, PersonalizedFeedResponse.class);
            
            if (cached.isPresent()) {
                log.debug("Cache hit for user: {}", userId);
                PersonalizedFeedResponse feed = cached.get();
                feed.setSource("CACHE");
                return feed;
            }
            
            log.debug("Cache miss for user: {}", userId);
            return null;
        } catch (Exception e) {
            log.error("Error retrieving feed from cache for user: {}", userId, e);
            return null;
        }
    }

    /**
     * Get feed from database precomputed items
     */
    private PersonalizedFeedResponse getFeedFromDatabase(UUID userId, int mentorLimit, int courseLimit, int jobLimit) {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Check if valid feed items exist
            boolean hasValidItems = precomputedFeedItemRepository.existsValidFeedItemsByUserId(userId, now);
            if (!hasValidItems) {
                log.debug("No valid precomputed feed items found in database for user: {}", userId);
                return null;
            }

            // Get feed items by type
            List<PrecomputedFeedItem> mentorItems = precomputedFeedItemRepository
                .findValidFeedItemsByUserIdAndType(userId, FeedItemType.MENTOR, now)
                .stream()
                .limit(mentorLimit)
                .collect(Collectors.toList());

            List<PrecomputedFeedItem> courseItems = precomputedFeedItemRepository
                .findValidFeedItemsByUserIdAndType(userId, FeedItemType.COURSE, now)
                .stream()
                .limit(courseLimit)
                .collect(Collectors.toList());

            List<PrecomputedFeedItem> jobItems = precomputedFeedItemRepository
                .findValidFeedItemsByUserIdAndType(userId, FeedItemType.JOB, now)
                .stream()
                .limit(jobLimit)
                .collect(Collectors.toList());

            // Convert to response DTOs by fetching actual data
            List<MentorRecommendationResponse> mentors = fetchMentorDetails(mentorItems);
            List<CourseRecommendationResponse> courses = fetchCourseDetails(courseItems);
            List<JobRecommendationResponse> jobs = fetchJobDetails(jobItems);

            // Build response
            PersonalizedFeedResponse feed = PersonalizedFeedResponse.builder()
                .mentors(mentors)
                .courses(courses)
                .jobs(jobs)
                .generatedAt(LocalDateTime.now())
                .source("DATABASE")
                .totalItems(mentors.size() + courses.size() + jobs.size())
                .isRealTime(false)
                .build();

            log.debug("Retrieved {} items from database for user: {}", feed.getTotalItems(), userId);
            return feed;

        } catch (Exception e) {
            log.error("Error retrieving feed from database for user: {}", userId, e);
            return null;
        }
    }

    /**
     * Compute feed in real-time using recommendation services
     */
    private PersonalizedFeedResponse computeFeedRealTime(UUID userId, int mentorLimit, int courseLimit, int jobLimit) {
        log.debug("Computing feed in real-time for user: {}", userId);

        try {
            // Call all recommendation services
            List<MentorRecommendationResponse> mentors = mentorRecommendationService
                .getRecommendedMentors(userId, mentorLimit);

            List<CourseRecommendationResponse> courses = courseRecommendationService
                .getRecommendedCourses(userId, courseLimit);

            List<JobRecommendationResponse> jobs = jobRecommendationService
                .getRecommendedJobs(userId, jobLimit);

            // Build response
            PersonalizedFeedResponse feed = PersonalizedFeedResponse.builder()
                .mentors(mentors)
                .courses(courses)
                .jobs(jobs)
                .generatedAt(LocalDateTime.now())
                .source("REAL_TIME")
                .totalItems(mentors.size() + courses.size() + jobs.size())
                .isRealTime(true)
                .build();

            log.debug("Computed {} items in real-time for user: {}", feed.getTotalItems(), userId);
            return feed;

        } catch (Exception e) {
            log.error("Error computing feed in real-time for user: {}, falling back to popular content", userId, e);
            
            // Fallback to popular/trending content when real-time computation fails
            // Requirement 14.1, 14.4: Display popular content when Matching Engine unavailable
            return getPopularContentFallback(mentorLimit, courseLimit, jobLimit);
        }
    }

    /**
     * Get popular/trending content as fallback when personalized recommendations fail
     * Requirement 14.1: Fallback to popular content when Matching Engine unavailable
     * Requirement 14.4: Fallback to trending content when real-time computation fails
     */
    private PersonalizedFeedResponse getPopularContentFallback(int mentorLimit, int courseLimit, int jobLimit) {
        log.info("Fetching popular content as fallback (mentors: {}, courses: {}, jobs: {})", 
                 mentorLimit, courseLimit, jobLimit);

        List<MentorRecommendationResponse> mentors = new ArrayList<>();
        List<CourseRecommendationResponse> courses = new ArrayList<>();
        List<JobRecommendationResponse> jobs = new ArrayList<>();

        try {
            // Get popular mentors (featured or highest rated)
            mentors = getPopularMentors(mentorLimit);
        } catch (Exception e) {
            log.error("Error fetching popular mentors for fallback", e);
        }

        try {
            // Get popular courses (highest enrollments or ratings)
            courses = getPopularCourses(courseLimit);
        } catch (Exception e) {
            log.error("Error fetching popular courses for fallback", e);
        }

        try {
            // Get trending jobs (most recent or most proposals)
            jobs = getTrendingJobs(jobLimit);
        } catch (Exception e) {
            log.error("Error fetching trending jobs for fallback", e);
        }

        PersonalizedFeedResponse feed = PersonalizedFeedResponse.builder()
            .mentors(mentors)
            .courses(courses)
            .jobs(jobs)
            .generatedAt(LocalDateTime.now())
            .source("POPULAR_FALLBACK")
            .totalItems(mentors.size() + courses.size() + jobs.size())
            .isRealTime(false)
            .build();

        log.info("Returning {} popular items as fallback", feed.getTotalItems());
        return feed;
    }

    /**
     * Get popular mentors (featured or highest rated)
     * Returns mentors without personalized match scores
     */
    private List<MentorRecommendationResponse> getPopularMentors(int limit) {
        try {
            // Get featured mentors first, then top-rated mentors
            List<MentorProfile> featuredMentors = mentorProfileRepository.findFeatured();
            
            // If not enough featured mentors, get more from approved mentors
            if (featuredMentors.size() < limit) {
                Pageable pageable = PageRequest.of(0, limit);
                List<MentorProfile> approvedMentors = mentorProfileRepository.findApproved(pageable).getContent();
                
                // Combine featured and approved, removing duplicates
                Set<UUID> featuredIds = featuredMentors.stream()
                    .map(MentorProfile::getId)
                    .collect(Collectors.toSet());
                
                approvedMentors.stream()
                    .filter(m -> !featuredIds.contains(m.getId()))
                    .forEach(featuredMentors::add);
            }

            // Convert to response DTOs with generic match score
            BigDecimal genericScore = new BigDecimal("90.00");
            
            return featuredMentors.stream()
                .limit(limit)
                .map(mentor -> convertMentorToResponse(mentor, genericScore))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error fetching popular mentors", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get popular courses (highest enrollments or ratings)
     * Returns courses without personalized match scores
     */
    private List<CourseRecommendationResponse> getPopularCourses(int limit) {
        try {
            // Get published courses sorted by enrollments (implicit in pagination)
            Pageable pageable = PageRequest.of(0, limit);
            List<Course> courses = courseRepository
                .findByStatusAndDeletedAtIsNull(CourseStatus.PUBLISHED, pageable)
                .getContent();

            // Convert to response DTOs with generic match score
            BigDecimal genericScore = new BigDecimal("90.00");
            
            return courses.stream()
                .map(course -> convertCourseToResponse(course, genericScore))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error fetching popular courses", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get trending jobs (most recent or most proposals)
     * Returns jobs without personalized match scores
     */
    private List<JobRecommendationResponse> getTrendingJobs(int limit) {
        try {
            // Get open jobs (most recent first)
            Pageable pageable = PageRequest.of(0, limit);
            List<Job> jobs = jobRepository.findOpen(pageable).getContent();

            // Convert to response DTOs with generic match score
            BigDecimal genericScore = new BigDecimal("90.00");
            
            return jobs.stream()
                .map(job -> convertJobToResponse(job, genericScore))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error fetching trending jobs", e);
            return Collections.emptyList();
        }
    }

    /**
     * Convert MentorProfile to MentorRecommendationResponse
     */
    private MentorRecommendationResponse convertMentorToResponse(MentorProfile mentor, BigDecimal matchScore) {
        try {
            User mentorUser = mentor.getUser();
            
            // Get mentor's skills
            List<UserSkill> mentorSkills = userSkillRepository.findByUserId(mentorUser.getId());
            List<String> skillNames = mentorSkills.stream()
                .map(skill -> skill.getSkill().getLabelEn())
                .distinct()
                .collect(Collectors.toList());

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
                .skills(skillNames)
                .categories(skillNames) // Using skills as categories for simplicity
                .matchScore(matchScore)
                .isFeatured(mentor.getIsFeatured())
                .isAvailable("Available".equalsIgnoreCase(mentor.getAvailability()))
                .build();
        } catch (Exception e) {
            log.error("Error converting mentor {} to response", mentor.getId(), e);
            return null;
        }
    }

    /**
     * Convert Course to CourseRecommendationResponse
     */
    private CourseRecommendationResponse convertCourseToResponse(Course course, BigDecimal matchScore) {
        try {
            String categoryName = null;
            if (course.getCategoryId() != null) {
                categoryName = categoryRepository.findById(course.getCategoryId())
                    .map(Category::getLabelEn)
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
                .language(course.getLanguage().name())
                .skills(Collections.emptyList())
                .categoryId(course.getCategoryId())
                .categoryName(categoryName)
                .matchScore(matchScore)
                .isCertificate(course.getIsCertificate())
                .build();
        } catch (Exception e) {
            log.error("Error converting course {} to response", course.getId(), e);
            return null;
        }
    }

    /**
     * Convert Job to JobRecommendationResponse
     */
    private JobRecommendationResponse convertJobToResponse(Job job, BigDecimal matchScore) {
        try {
            String categoryName = null;
            if (job.getCategoryId() != null) {
                categoryName = categoryRepository.findById(job.getCategoryId())
                    .map(Category::getLabelEn)
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
                .requiredSkills(Collections.emptyList())
                .proposalCount(job.getProposalCount())
                .publishedAt(job.getPublishedAt())
                .matchScore(matchScore)
                .isFeatured(job.getIsFeatured())
                .build();
        } catch (Exception e) {
            log.error("Error converting job {} to response", job.getId(), e);
            return null;
        }
    }

    /**
     * Cache feed in Redis
     */
    private void cacheFeed(UUID userId, PersonalizedFeedResponse feed) {
        try {
            String cacheKey = cacheService.generateFeedKey(userId);
            cacheService.set(cacheKey, feed);
            log.debug("Cached feed for user: {}", userId);
        } catch (Exception e) {
            log.error("Error caching feed for user: {}", userId, e);
            // Don't throw - cache failures should not break the flow
        }
    }

    /**
     * Store feed items in database
     */
    @Transactional
    private void storeFeedInDatabase(UUID userId, PersonalizedFeedResponse feed) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusHours(24);

            List<PrecomputedFeedItem> items = new ArrayList<>();

            // Store mentor recommendations
            for (MentorRecommendationResponse mentor : feed.getMentors()) {
                PrecomputedFeedItem item = new PrecomputedFeedItem();
                item.setUser(user);
                item.setItemType(FeedItemType.MENTOR);
                item.setItemId(mentor.getMentorId());
                item.setMatchScore(mentor.getMatchScore());
                item.setComputedAt(now);
                item.setExpiresAt(expiresAt);
                items.add(item);
            }

            // Store course recommendations
            for (CourseRecommendationResponse course : feed.getCourses()) {
                PrecomputedFeedItem item = new PrecomputedFeedItem();
                item.setUser(user);
                item.setItemType(FeedItemType.COURSE);
                item.setItemId(course.getCourseId());
                item.setMatchScore(course.getMatchScore());
                item.setComputedAt(now);
                item.setExpiresAt(expiresAt);
                items.add(item);
            }

            // Store job recommendations
            for (JobRecommendationResponse job : feed.getJobs()) {
                PrecomputedFeedItem item = new PrecomputedFeedItem();
                item.setUser(user);
                item.setItemType(FeedItemType.JOB);
                item.setItemId(job.getJobId());
                item.setMatchScore(job.getMatchScore());
                item.setComputedAt(now);
                item.setExpiresAt(expiresAt);
                items.add(item);
            }

            precomputedFeedItemRepository.saveAll(items);
            log.debug("Stored {} feed items in database for user: {}", items.size(), userId);

        } catch (Exception e) {
            log.error("Error storing feed in database for user: {}", userId, e);
            // Don't throw - storage failures should not break the flow
        }
    }

    /**
     * Fetch mentor details from precomputed items
     */
    private List<MentorRecommendationResponse> fetchMentorDetails(List<PrecomputedFeedItem> items) {
        List<MentorRecommendationResponse> mentors = new ArrayList<>();
        
        for (PrecomputedFeedItem item : items) {
            try {
                MentorRecommendationResponse mentor = mentorRecommendationService
                    .calculateMentorMatch(item.getUser().getId(), item.getItemId());
                
                if (mentor != null) {
                    // Use the stored match score (it might be slightly different from recalculated)
                    mentor.setMatchScore(item.getMatchScore());
                    mentors.add(mentor);
                }
            } catch (Exception e) {
                log.error("Error fetching mentor details for item: {}", item.getItemId(), e);
            }
        }
        
        return mentors;
    }

    /**
     * Fetch course details from precomputed items
     */
    private List<CourseRecommendationResponse> fetchCourseDetails(List<PrecomputedFeedItem> items) {
        List<CourseRecommendationResponse> courses = new ArrayList<>();
        
        for (PrecomputedFeedItem item : items) {
            try {
                CourseRecommendationResponse course = courseRecommendationService
                    .calculateCourseMatch(item.getUser().getId(), item.getItemId());
                
                if (course != null) {
                    // Use the stored match score
                    course.setMatchScore(item.getMatchScore());
                    courses.add(course);
                }
            } catch (Exception e) {
                log.error("Error fetching course details for item: {}", item.getItemId(), e);
            }
        }
        
        return courses;
    }

    /**
     * Fetch job details from precomputed items
     */
    private List<JobRecommendationResponse> fetchJobDetails(List<PrecomputedFeedItem> items) {
        List<JobRecommendationResponse> jobs = new ArrayList<>();
        
        for (PrecomputedFeedItem item : items) {
            try {
                JobRecommendationResponse job = jobRecommendationService
                    .calculateJobMatch(item.getUser().getId(), item.getItemId());
                
                if (job != null) {
                    // Use the stored match score
                    job.setMatchScore(item.getMatchScore());
                    jobs.add(job);
                }
            } catch (Exception e) {
                log.error("Error fetching job details for item: {}", item.getItemId(), e);
            }
        }
        
        return jobs;
    }

    /**
     * Limit feed items to specified counts
     */
    private PersonalizedFeedResponse limitFeedItems(PersonalizedFeedResponse feed, 
                                                     int mentorLimit, int courseLimit, int jobLimit) {
        List<MentorRecommendationResponse> limitedMentors = feed.getMentors().stream()
            .limit(mentorLimit)
            .collect(Collectors.toList());

        List<CourseRecommendationResponse> limitedCourses = feed.getCourses().stream()
            .limit(courseLimit)
            .collect(Collectors.toList());

        List<JobRecommendationResponse> limitedJobs = feed.getJobs().stream()
            .limit(jobLimit)
            .collect(Collectors.toList());

        return PersonalizedFeedResponse.builder()
            .mentors(limitedMentors)
            .courses(limitedCourses)
            .jobs(limitedJobs)
            .generatedAt(feed.getGeneratedAt())
            .source(feed.getSource())
            .totalItems(limitedMentors.size() + limitedCourses.size() + limitedJobs.size())
            .isRealTime(feed.getIsRealTime())
            .build();
    }
}
