package com.mentorx.api.feature.course.dto.request;

import com.mentorx.api.common.enums.SupportedLanguage;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCreateRequest {

    @NotNull(message = "Instructor ID is required")
    private java.util.UUID instructorId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Slug is required")
    @Size(max = 300, message = "Slug must not exceed 300 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    private String slug;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Integer categoryId;

    private String thumbnailUrl;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    private BigDecimal priceMxc;

    @NotNull(message = "Language is required")
    private SupportedLanguage language;

    @Size(max = 20, message = "Level must not exceed 20 characters")
    private String level;

    private Boolean isCertificate;

    private String previewVideoUrl;
}
