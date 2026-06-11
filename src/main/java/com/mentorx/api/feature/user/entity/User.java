package com.mentorx.api.feature.user.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.SupportedLanguage;
import com.mentorx.api.common.enums.UserStatus;
import com.mentorx.api.feature.user.onboarding.model.OnboardingJsonState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 30)
    private String phone;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_language")
    @Builder.Default
    private SupportedLanguage preferredLanguage = SupportedLanguage.en;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(name = "is_mentor", nullable = false)
    @Builder.Default
    private Boolean isMentor = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "mentor_status", nullable = false)
    @Builder.Default
    private MentorStatus mentorStatus = MentorStatus.NONE;

    @Column(name = "is_2fa_enabled", nullable = false)
    @Builder.Default
    private Boolean is2faEnabled = false;

    @Column(name = "totp_secret", length = 64)
    private String totpSecret;

    @Column(name = "profile_is_public", nullable = false)
    @Builder.Default
    private Boolean profileIsPublic = true;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "onboarding_state", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private OnboardingJsonState onboardingState;

    @Column(name = "is_onboarded", nullable = false)
    @Builder.Default
    private Boolean isOnboarded = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserRole> userRoles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MentorProfile mentorProfile;
}
