package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.BudgetType;
import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
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

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "custom_category_name", length = 120)
    private String customCategoryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private JobType jobType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "job_required_skills",
            joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill", length = 120)
    @Builder.Default
    private List<String> requiredSkills = new ArrayList<>();

    @Column(name = "experience_level", length = 80)
    private String experienceLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", length = 120)
    private com.mentorx.api.common.enums.UserLevel currentLevel;

    @Column(name = "learning_goals", columnDefinition = "TEXT")
    private String learningGoals;

    @Column(name = "success_criteria", columnDefinition = "TEXT")
    private String successCriteria;

    @Column(name = "availability_expectation", length = 255)
    private String availabilityExpectation;

    @Column(name = "availability_start_time", length = 120)
    private String availabilityStartTime;

    @Column(name = "availability_end_time", length = 120)
    private String availabilityEndTime;

    @Column(name = "communication_preference", length = 120)
    private String communicationPreference;
    
    @Column(name = "timezone", length = 100)
    private String timezone;

    @Column(name = "expected_sessions")
    private Integer expectedSessions;

    @Column(name = "expected_weeks")
    private Integer expectedWeeks;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 50)
    @Builder.Default
    private com.mentorx.api.common.enums.JobVisibility visibility = com.mentorx.api.common.enums.JobVisibility.PUBLIC;

    @Column(name = "preferred_language", length = 50)
    private String preferredLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_type", nullable = false)
    private BudgetType budgetType;

    @Column(name = "budget_min_mxc", precision = 12, scale = 2)
    private BigDecimal budgetMinMxc;

    @Column(name = "budget_max_mxc", precision = 12, scale = 2)
    private BigDecimal budgetMaxMxc;

    @Column(name = "hourly_rate_mxc", precision = 12, scale = 2)
    private BigDecimal hourlyRateMxc;

    @Column(name = "estimated_hours", precision = 6, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private JobStatus status = JobStatus.DRAFT;

    @Builder.Default
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Builder.Default
    @Column(name = "proposal_count", nullable = false)
    private Integer proposalCount = 0;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "status_reason", columnDefinition = "TEXT")
    private String statusReason;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_attachments_list", 
                    joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "attachment_url", length = 500)
    @Builder.Default
    private List<String> attachments = new ArrayList<>();
}
