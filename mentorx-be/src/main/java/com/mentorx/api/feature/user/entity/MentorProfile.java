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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Column(name = "video_intro_url")
    private String videoIntroUrl;

    @Column(length = 150)
    private String location;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> languages;

    @Column(name = "legal_name", length = 150)
    private String legalName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "country_of_residence", length = 100)
    private String countryOfResidence;

    @Column(name = "identity_document_type", length = 50)
    private String identityDocumentType;

    @Column(name = "identity_document_url")
    private String identityDocumentUrl;

    @Column(name = "portrait_url")
    private String portraitUrl;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    @Column(name = "current_title", length = 150)
    private String currentTitle;

    @Column(name = "current_company", length = 150)
    private String currentCompany;

    @Column(name = "primary_domain", length = 120)
    private String primaryDomain;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "portfolio_evidence_url")
    private String portfolioEvidenceUrl;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @Column(name = "bank_account_name", length = 150)
    private String bankAccountName;

    @Column(name = "bank_name", length = 150)
    private String bankName;

    @Column(name = "bank_account_number", length = 80)
    private String bankAccountNumber;

    @Column(name = "bank_branch", length = 150)
    private String bankBranch;

    @Column(name = "tax_id", length = 80)
    private String taxId;

    @Column(name = "mentor_agreement_accepted", nullable = false)
    @Builder.Default
    private Boolean mentorAgreementAccepted = false;

    @Column(name = "dispute_policy_accepted", nullable = false)
    @Builder.Default
    private Boolean disputePolicyAccepted = false;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "identity_document_back_url")
    private String identityDocumentBackUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "verification_metadata", columnDefinition = "jsonb")
    private String verificationMetadata;

    @Column(name = "rejection_reason")
    private String rejectionReason;
}
