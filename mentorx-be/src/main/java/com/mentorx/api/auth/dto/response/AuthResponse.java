package com.mentorx.api.auth.dto.response;

import com.mentorx.api.feature.user.dto.response.UserResponse;
import lombok.Builder;

@Builder
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn,
    UserResponse user
) {}