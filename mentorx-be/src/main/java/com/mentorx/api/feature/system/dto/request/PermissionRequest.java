package com.mentorx.api.feature.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PermissionRequest(
    @NotBlank(message = "Permission key is required")
    @Size(max = 100, message = "Permission key must not exceed 100 characters")
    @Pattern(regexp = "^[a-z_:]+$", message = "Permission key must contain only lowercase letters, underscores, and colons")
    String permissionKey,
    
    String description
) {}
