package com.mentorx.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GithubLoginRequest {
    @NotBlank(message = "GitHub authorization code is required")
    private String code;
}
