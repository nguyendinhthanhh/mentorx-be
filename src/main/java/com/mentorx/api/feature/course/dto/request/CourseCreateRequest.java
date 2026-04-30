package com.mentorx.api.feature.course.dto.request;

import com.mentorx.api.common.enums.SupportedLanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CourseCreateRequest(
        @NotNull UUID instructorId,
        Integer categoryId,
        @NotBlank String title,
        @NotBlank String slug,
        String description,
        String thumbnailUrl,
        BigDecimal priceMxc,
        SupportedLanguage language,
        String level,
        Boolean isCertificate,
        String previewVideoUrl
) {}
