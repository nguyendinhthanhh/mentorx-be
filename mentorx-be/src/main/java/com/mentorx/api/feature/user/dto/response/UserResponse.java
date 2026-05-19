package com.mentorx.api.feature.user.dto.response;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.SupportedLanguage;
import com.mentorx.api.common.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String fullName,
    String displayName,
    String avatarUrl,
    String bio,
    String phone,
    String countryCode,
    SupportedLanguage preferredLanguage,
    UserStatus status,
    Boolean isEmailVerified,
    Boolean isMentor,
    MentorStatus mentorStatus,
    Boolean is2faEnabled,
    Boolean profileIsPublic,
    Boolean isOnboarded,
    LocalDateTime lastSeenAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<UserRoleResponse> roles,
    MentorProfileResponse mentorProfile
) {}