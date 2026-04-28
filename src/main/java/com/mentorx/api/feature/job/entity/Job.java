package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.BudgetType;
import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_type", nullable = false)
    private BudgetType budgetType;

    @Column(name = "budget_amount", precision = 10, scale = 2)
    private BigDecimal budgetAmount;

    @Column(name = "hourly_rate_min", precision = 10, scale = 2)
    private BigDecimal hourlyRateMin;

    @Column(name = "hourly_rate_max", precision = 10, scale = 2)
    private BigDecimal hourlyRateMax;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "skills_required", columnDefinition = "TEXT")
    private String skillsRequired;

    @Column(name = "experience_level", length = 50)
    private String experienceLevel;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JobStatus status = JobStatus.DRAFT;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_urgent", nullable = false)
    @Builder.Default
    private Boolean isUrgent = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "subcategory", length = 100)
    private String subcategory;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "is_remote", nullable = false)
    @Builder.Default
    private Boolean isRemote = true;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments; // JSON array of file URLs

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "proposal_count", nullable = false)
    @Builder.Default
    private Integer proposalCount = 0;

    // Relationships
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JobProposal> proposals;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JobContract> contracts;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JobView> jobViews;
}