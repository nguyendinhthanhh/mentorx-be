package com.mentorx.api.feature.user.dto.request;

import com.mentorx.api.common.enums.SupportedLanguage;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    String fullName,

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    String displayName,

    String avatarUrl,

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    String bio,

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    String phone,

    @Size(max = 2, message = "Country code must be 2 characters")
    String countryCode,

    SupportedLanguage preferredLanguage,

    Boolean profileIsPublic
) {}