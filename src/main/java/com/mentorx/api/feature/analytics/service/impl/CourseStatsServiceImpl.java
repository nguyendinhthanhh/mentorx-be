package com.mentorx.api.feature.analytics.service.impl;

import com.mentorx.api.feature.analytics.dto.response.CourseStatsResponse;
import com.mentorx.api.feature.analytics.dto.response.CourseStatsResponse.CourseBreakdown;
import com.mentorx.api.feature.analytics.service.CourseStatsService;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.entity.CourseEnrollment;
import com.mentorx.api.feature.course.repository.CourseEnrollmentRepository;
import com.mentorx.api.feature.course.repository.CourseLessonRepository;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.review.enums.ReviewTargetType;
import com.mentorx.api.feature.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseStatsServiceImpl implements CourseStatsService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseLessonRepository lessonRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public CourseStatsResponse getStats(UUID userId, UUID courseId) {
        List<Course> courses = listCoursesForInstructor(userId, courseId);

        if (courses.isEmpty()) {
            return new CourseStatsResponse(
                    userId, 0, 0, BigDecimal.ZERO, 0, 0.0, 0L, List.of()
            );
        }

        int totalCourses = courses.size();
        int totalSold = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalEnrollments = 0;
        long totalLessonViews = 0;
        double weightedCompletion = 0.0;
        long completionWeight = 0;
        List<CourseBreakdown> breakdowns = new ArrayList<>(totalCourses);

        for (Course c : courses) {
            UUID cid = c.getId();
            long sold = enrollmentRepository.countByCourseId(cid);
            long completed = enrollmentRepository.countCompletedByCourseId(cid);
            long enrolled = enrollmentRepository.countByCourseId(cid);
            Double avgProgress = enrollmentRepository.getAverageProgressByCourseId(cid);
            BigDecimal allRevenue = sumRevenueForCourse(cid);

            Long lessonViews = lessonRepository.sumViewCountByCourseId(cid);
            BigDecimal avgRating = reviewRepository.averagePublicRatingByTarget(
                    ReviewTargetType.COURSE, cid);

            double completion = enrolled == 0 ? 0.0 :
                    BigDecimal.valueOf(completed)
                            .divide(BigDecimal.valueOf(enrolled), 4, RoundingMode.HALF_UP)
                            .doubleValue();

            totalSold += (int) sold;
            totalRevenue = totalRevenue.add(allRevenue);
            totalEnrollments += enrolled;
            totalLessonViews += lessonViews == null ? 0 : lessonViews;
            weightedCompletion += completion * enrolled;
            completionWeight += enrolled;

            breakdowns.add(new CourseBreakdown(
                    cid,
                    c.getTitle(),
                    (int) sold,
                    allRevenue,
                    (int) enrolled,
                    completion,
                    lessonViews == null ? 0L : lessonViews,
                    avgRating == null ? 0.0 : avgRating.doubleValue()
            ));
        }

        double averageCompletion = completionWeight == 0 ? 0.0 :
                BigDecimal.valueOf(weightedCompletion)
                        .divide(BigDecimal.valueOf(completionWeight), 4, RoundingMode.HALF_UP)
                        .doubleValue();

        return new CourseStatsResponse(
                userId, totalCourses, totalSold, totalRevenue,
                (int) totalEnrollments, averageCompletion, totalLessonViews, breakdowns
        );
    }

    private BigDecimal sumRevenueForCourse(UUID courseId) {
        long count = enrollmentRepository.countByCourseId(courseId);
        if (count == 0) return BigDecimal.ZERO;
        Page<CourseEnrollment> page = enrollmentRepository.findByCourseId(
                courseId, PageRequest.of(0, Integer.MAX_VALUE));
        return page.getContent().stream()
                .map(e -> e.getAmountPaidMxc() == null ? BigDecimal.ZERO : e.getAmountPaidMxc())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Course> listCoursesForInstructor(UUID userId, UUID courseId) {
        if (courseId != null) {
            return courseRepository.findById(courseId)
                    .filter(c -> c.getInstructor() != null
                            && userId.equals(c.getInstructor().getId()))
                    .map(List::of)
                    .orElse(List.of());
        }
        Page<Course> page = courseRepository.findByInstructorIdAndDeletedAtIsNull(
                userId, PageRequest.of(0, Integer.MAX_VALUE));
        return page.getContent();
    }
}
