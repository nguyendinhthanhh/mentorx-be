package com.mentorx.api.feature.mentor.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "mentor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "professional_title", length = 200)
    private String professionalTitle;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "expertise_summary", columnDefinition = "TEXT")
    private String expertiseSummary;

    @Column(name = "teaching_approach", columnDefinition = "TEXT")
    private String teachingApproach;

    @Column(name = "availability_note", columnDefinition = "TEXT")
    private String availabilityNote;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @Column(name = "video_intro_url")
    private String videoIntroUrl;

    @Column(name = "is_available_for_jobs", nullable = false)
    @Builder.Default
    private Boolean isAvailableForJobs = true;

    @Column(name = "is_available_for_courses", nullable = false)
    @Builder.Default
    private Boolean isAvailableForCourses = true;

    @Column(name = "max_concurrent_students")
    private Integer maxConcurrentStudents;

    @Column(name = "response_time_hours")
    private Integer responseTimeHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private MentorStatus status = MentorStatus.PENDING;

    @Column(name = "application_note", columnDefinition = "TEXT")
    private String applicationNote;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    // Relationships
    @OneToMany(mappedBy = "mentorProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MentorSkill> mentorSkills;

    @OneToMany(mappedBy = "mentorProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MentorAvailability> mentorAvailabilities;

    @OneToMany(mappedBy = "mentorProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MentorCertification> mentorCertifications;
}