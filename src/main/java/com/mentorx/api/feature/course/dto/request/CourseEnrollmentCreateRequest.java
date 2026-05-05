package com.mentorx.api.feature.course.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class CourseEnrollmentCreateRequest {

    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Amount paid must be greater than or equal to 0")
    private BigDecimal amountPaidMxc;
}
