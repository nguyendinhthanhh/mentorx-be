package com.mentorx.api.feature.user.dto.request;

import com.mentorx.api.common.enums.SupportedLanguage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    String password,

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    String fullName,

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    String displayName,

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    String bio,

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    String phone,

    @Size(max = 2, message = "Country code must be 2 characters")
    String countryCode,

    SupportedLanguage preferredLanguage
) {}