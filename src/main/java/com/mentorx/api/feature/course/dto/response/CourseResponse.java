package com.mentorx.api.feature.course.dto.response;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.enums.CourseProductType;
import com.mentorx.api.common.enums.SupportedLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {

    private UUID id;
    private UUID instructorId;
    private String instructorName;
    private Integer categoryId;
    private List<String> skills;
    private List<Integer> skillIds;
    private String title;
    private String slug;
    private String description;
    private String thumbnailUrl;
    private BigDecimal priceMxc;
    private CourseStatus status;
    private SupportedLanguage language;
    private String level;
    private Integer totalDurationMin;
    private Short totalLessons;
    private Integer totalEnrollments;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Boolean isCertificate;
    private String previewVideoUrl;
    private CourseProductType productType;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime publishedAt;
    private UUID reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public UUID getCourseId() {
        return id;
    }
}
