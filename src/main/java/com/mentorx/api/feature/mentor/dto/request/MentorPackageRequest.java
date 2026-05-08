package com.mentorx.api.feature.mentor.dto.request;

import com.mentorx.api.common.enums.PackageType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorPackageRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Package type is required")
    private PackageType packageType;

    @NotNull(message = "Duration hours is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    private Integer durationHours;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal priceMxc;

    private List<String> features;

    private Boolean isActive = true;

    @Min(value = 0, message = "Display order must be non-negative")
    private Integer displayOrder = 0;
}
