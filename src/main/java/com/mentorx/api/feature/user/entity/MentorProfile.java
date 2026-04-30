package com.mentorx.api.feature.user.entity;

import com.mentorx.api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 255)
    private String headline;

    @Column(name = "hourly_rate_mxc", precision = 12, scale = 2)
    private BigDecimal hourlyRateMxc;

    @Column(name = "years_of_experience")
    private Short yearsOfExperience;

    @Column(length = 50)
    private String availability;

    @Column(name = "response_time_hours")
    private Short responseTimeHours;

    @Column(name = "total_jobs_done", nullable = false)
    @Builder.Default
    private Integer totalJobsDone = 0;

    @Column(name = "success_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal successRate = BigDecimal.ZERO;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "cv_url")
    private String cvUrl;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;
}
