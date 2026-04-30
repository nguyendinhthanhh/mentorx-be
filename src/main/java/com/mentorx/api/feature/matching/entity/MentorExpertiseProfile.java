package com.mentorx.api.feature.matching.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.system.entity.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing mentor expertise profile for matching algorithms
 * Tracks mentor skills, experience levels, and teaching capabilities
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "mentor_expertise_profiles", indexes = {
    @Index(name = "idx_mentor_expertise_mentor_id", columnList = "mentor_profile_id"),
    @Index(name = "idx_mentor_expertise_category_id", columnList = "category_id"),
    @Index(name = "idx_mentor_expertise_score", columnList = "expertise_score DESC"),
    @Index(name = "idx_mentor_expertise_verified", columnList = "is_verified, expertise_score DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MentorExpertiseProfile extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_profile_id", nullable = false)
    private MentorProfile mentorProfile;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Expertise score from 0.0 to 1.0 indicating mentor's skill level
     */
    @NotNull
    @DecimalMin(value = "0.0", message = "Expertise score must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Expertise score must not exceed 1.0")
    @Column(name = "expertise_score", precision = 5, scale = 4, nullable = false)
    private BigDecimal expertiseScore;

    /**
     * Years of experience in this category
     */
    @DecimalMin(value = "0.0")
    @Column(name = "years_of_experience", precision = 4, scale = 1)
    private BigDecimal yearsOfExperience;

    /**
     * Number of successful projects/mentoring sessions completed
     */
    @Column(name = "completed_projects", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer completedProjects = 0;

    /**
     * Average rating received for work in this category
     */
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    /**
     * Total number of ratings received in this category
     */
    @Column(name = "rating_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer ratingCount = 0;

    /**
     * Whether this expertise has been verified by platform
     */
    @Column(name = "is_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isVerified = false;

    /**
     * Date when expertise was verified
     */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    /**
     * ID of admin who verified this expertise
     */
    @Column(name = "verified_by_admin_id")
    private Long verifiedByAdminId;

    /**
     * Hourly rate for mentoring in this category (in MXC)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "hourly_rate_mxc", precision = 10, scale = 2)
    private BigDecimal hourlyRateMxc;

    /**
     * Whether mentor is currently available for work in this category
     */
    @Column(name = "is_available", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isAvailable = true;

    /**
     * When this expertise profile was last updated
     */
    @NotNull
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}