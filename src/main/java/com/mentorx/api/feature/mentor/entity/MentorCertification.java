package com.mentorx.api.feature.mentor.entity;

import com.mentorx.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "mentor_certifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorCertification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_profile_id", nullable = false)
    private MentorProfile mentorProfile;

    @Column(name = "certification_name", nullable = false, length = 200)
    private String certificationName;

    @Column(name = "issuing_organization", nullable = false, length = 200)
    private String issuingOrganization;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "credential_id", length = 100)
    private String credentialId;

    @Column(name = "credential_url")
    private String credentialUrl;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;
}