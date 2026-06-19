package com.mentorx.api.feature.analytics.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CourseStatsResponse(
        UUID userId,
        Integer totalCourses,
        Integer totalCoursesSold,
        BigDecimal totalRevenueMxc,
        Integer totalEnrollments,
        Double averageCompletionRate,
        Long totalLessonViews,
        List<CourseBreakdown> courses
) {
    public record CourseBreakdown(
            UUID courseId,
            String title,
            Integer coursesSold,
            BigDecimal revenueMxc,
            Integer enrollments,
            Double completionRate,
            Long lessonViews,
            Double averageRating
    ) {}
}
