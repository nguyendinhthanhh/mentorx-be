package com.mentorx.api.feature.course.dto.response;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.enums.SupportedLanguage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        UUID instructorId,
        String instructorName,
        Integer categoryId,
        String title,
        String slug,
        String description,
        String thumbnailUrl,
        BigDecimal priceMxc,
        CourseStatus status,
        SupportedLanguage language,
        String level,
        Integer totalDurationMin,
        Short totalLessons,
        Integer totalEnrollments,
        BigDecimal averageRating,
        Integer totalReviews,
        Boolean isCertificate,
        String previewVideoUrl,
        String rejectionReason,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
