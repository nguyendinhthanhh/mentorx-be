package com.mentorx.api.feature.course.dto.request;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.enums.SupportedLanguage;

import java.math.BigDecimal;

public record CourseUpdateRequest(
        Integer categoryId,
        String title,
        String description,
        String thumbnailUrl,
        BigDecimal priceMxc,
        SupportedLanguage language,
        String level,
        Boolean isCertificate,
        String previewVideoUrl,
        CourseStatus status
) {}
