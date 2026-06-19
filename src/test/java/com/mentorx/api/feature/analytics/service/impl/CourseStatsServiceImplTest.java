package com.mentorx.api.feature.analytics.service.impl;

import com.mentorx.api.feature.analytics.dto.response.CourseStatsResponse;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.repository.CourseEnrollmentRepository;
import com.mentorx.api.feature.course.repository.CourseLessonRepository;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.review.enums.ReviewTargetType;
import com.mentorx.api.feature.review.repository.ReviewRepository;
import com.mentorx.api.feature.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * M12.2 Phase H3: unit tests for CourseStatsServiceImpl.
 * Covers L9 fix (aggregate revenue query) and zero-enrollment edge case.
 */
class CourseStatsServiceImplTest {

    private CourseRepository courseRepository;
    private CourseEnrollmentRepository enrollmentRepository;
    private CourseLessonRepository lessonRepository;
    private ReviewRepository reviewRepository;

    private CourseStatsServiceImpl service;

    @BeforeEach
    void setUp() {
        courseRepository = mock(CourseRepository.class);
        enrollmentRepository = mock(CourseEnrollmentRepository.class);
        lessonRepository = mock(CourseLessonRepository.class);
        reviewRepository = mock(ReviewRepository.class);

        service = new CourseStatsServiceImpl(courseRepository, enrollmentRepository, lessonRepository, reviewRepository);
    }

    // ── H3.12: L9 regression — uses aggregate query, not findByCourseId(MAX_VALUE) ──

    @Test
    void getStats_usesAggregateRevenueQuery() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Course course = buildCourse(courseId, userId);

        when(courseRepository.findByInstructorIdAndDeletedAtIsNull(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(course)));
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(5L);
        when(enrollmentRepository.countCompletedByCourseId(courseId)).thenReturn(3L);
        when(enrollmentRepository.getAverageProgressByCourseId(courseId)).thenReturn(60.0);
        when(enrollmentRepository.sumRevenueByCourseId(courseId)).thenReturn(new BigDecimal("1500.00"));
        when(lessonRepository.sumViewCountByCourseId(courseId)).thenReturn(200L);
        when(reviewRepository.averagePublicRatingByTarget(ReviewTargetType.COURSE, courseId))
                .thenReturn(new BigDecimal("4.50"));

        CourseStatsResponse result = service.getStats(userId, null);

        // Verify sumRevenueByCourseId is called (not findByCourseId with MAX_VALUE)
        verify(enrollmentRepository).sumRevenueByCourseId(courseId);
        verify(enrollmentRepository, never()).findByCourseId(eq(courseId), any(Pageable.class));

        assertThat(result.totalRevenueMxc()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(result.totalCoursesSold()).isEqualTo(5);
    }

    // ── H3.13: zero-enrollment edge — no ArithmeticException ──────────────────

    @Test
    void getStats_completionRateHandlesZeroEnrollments() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Course course = buildCourse(courseId, userId);

        when(courseRepository.findByInstructorIdAndDeletedAtIsNull(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(course)));
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(0L);
        when(enrollmentRepository.countCompletedByCourseId(courseId)).thenReturn(0L);
        when(enrollmentRepository.getAverageProgressByCourseId(courseId)).thenReturn(null);
        when(enrollmentRepository.sumRevenueByCourseId(courseId)).thenReturn(BigDecimal.ZERO);
        when(lessonRepository.sumViewCountByCourseId(courseId)).thenReturn(0L);
        when(reviewRepository.averagePublicRatingByTarget(ReviewTargetType.COURSE, courseId)).thenReturn(null);

        CourseStatsResponse result = service.getStats(userId, null);

        // No exception thrown, completion rate is 0
        assertThat(result.totalCourses()).isEqualTo(1);
        assertThat(result.totalCoursesSold()).isZero();
        assertThat(result.totalRevenueMxc()).isEqualByComparingTo(BigDecimal.ZERO);
        // Verify breakdowns exist
        assertThat(result.courses()).hasSize(1);
        assertThat(result.courses().get(0).completionRate()).isZero();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Course buildCourse(UUID courseId, UUID instructorId) {
        User instructor = new User();
        instructor.setId(instructorId);
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Test Course");
        course.setInstructor(instructor);
        return course;
    }
}
