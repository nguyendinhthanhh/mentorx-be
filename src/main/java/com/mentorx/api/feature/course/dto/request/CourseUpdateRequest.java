package com.mentorx.api.feature.course.dto.request;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.enums.SupportedLanguage;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseUpdateRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Integer categoryId;

    private List<@Size(max = 120, message = "Skill must not exceed 120 characters") String> skills;

    private List<Integer> skillIds;

    private String thumbnailUrl;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 0, message = "Price must be a full number")
    private BigDecimal priceMxc;

    private CourseStatus status;

    private SupportedLanguage language;

    @Size(max = 20, message = "Level must not exceed 20 characters")
    private String level;

    private Boolean isCertificate;

    private String previewVideoUrl;

    private String rejectionReason;
}
