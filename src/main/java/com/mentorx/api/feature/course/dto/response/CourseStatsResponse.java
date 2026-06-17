package com.mentorx.api.feature.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseStatsResponse {
    private UUID courseId;
    private long totalEnrollments;
    private long completedEnrollments;
    private double completionRate;
    private BigDecimal totalRevenueMxc;
    private BigDecimal last7DaysRevenueMxc;
    private long last7DaysEnrollments;
    private BigDecimal previous7DaysRevenueMxc;
    private long previous7DaysEnrollments;
}
