package com.mentorx.api.feature.course.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.CourseProductType;
import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.enums.SupportedLanguage;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Entity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @Column(name = "category_id")
    private Integer categoryId;

    @ElementCollection
    @CollectionTable(name = "course_skills", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "skill", length = 120)
    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "course_skill_ids", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "skill_id", nullable = false)
    @Builder.Default
    private List<Integer> skillIds = new ArrayList<>();

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 300, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Builder.Default
    @Column(name = "price_mxc", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceMxc = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status = CourseStatus.PUBLISHED;

    @Column(name = "discount_price_mxc", precision = 12, scale = 2)
    private BigDecimal discountPriceMxc;

    @Column(name = "discount_start_at")
    private LocalDateTime discountStartAt;

    @Column(name = "discount_end_at")
    private LocalDateTime discountEndAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportedLanguage language = SupportedLanguage.vi;

    @Column(length = 20)
    private String level;

    @Builder.Default
    @Column(name = "total_duration_min", nullable = false)
    private Integer totalDurationMin = 0;

    @Builder.Default
    @Column(name = "total_lessons", nullable = false)
    private Short totalLessons = 0;

    @Builder.Default
    @Column(name = "total_enrollments", nullable = false)
    private Integer totalEnrollments = 0;

    @Builder.Default
    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_reviews", nullable = false)
    private Integer totalReviews = 0;

    @Builder.Default
    @Column(name = "is_certificate", nullable = false)
    private Boolean isCertificate = false;

    @Column(name = "preview_video_url")
    private String previewVideoUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private CourseProductType productType = CourseProductType.COURSE;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
