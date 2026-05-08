package com.mentorx.api.feature.mentor.dto.request;

import com.mentorx.api.common.enums.CourseLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorOfferingRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal priceMxc;

    @NotNull(message = "Duration hours is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    private Integer durationHours;

    @NotNull(message = "Course level is required")
    private CourseLevel level;

    @NotNull(message = "Lessons count is required")
    @Min(value = 1, message = "Lessons count must be at least 1")
    private Integer lessonsCount;

    @Size(max = 500, message = "Thumbnail URL must not exceed 500 characters")
    private String thumbnailUrl;
}

