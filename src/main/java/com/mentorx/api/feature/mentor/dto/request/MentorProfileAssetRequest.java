package com.mentorx.api.feature.mentor.dto.request;

import com.mentorx.api.feature.mentor.enums.MentorProfileAssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorProfileAssetRequest {

    @NotNull(message = "Asset type is required")
    private MentorProfileAssetType type;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String description;

    @Size(max = 150, message = "Issuer must not exceed 150 characters")
    private String issuer;

    private String fileUrl;

    private String iconUrl;

    private LocalDate issuedAt;

    private Boolean isFeatured = false;

    private Integer displayOrder = 0;
}
