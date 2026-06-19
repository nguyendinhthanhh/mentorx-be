package com.mentorx.api.feature.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollmentResponse {

    private UUID id;
    private UUID courseId;
    private String courseTitle;
    private UUID studentId;
    private String studentName;
    private BigDecimal amountPaidMxc;
    private BigDecimal progressPercent;
    private Boolean isCompleted;
    private String certificateUrl;
    private String certificateCode;
    private LocalDateTime certificateIssuedAt;
    private LocalDateTime enrolledAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime completedAt;
}
