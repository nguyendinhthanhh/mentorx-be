package com.mentorx.api.feature.mentor.dto.response;

import com.mentorx.api.common.enums.PackageType;
import com.mentorx.api.feature.mentor.entity.MentorPackage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorPackageResponse {

    private UUID id;
    private UUID mentorProfileId;
    private String title;
    private String description;
    private PackageType packageType;
    private Integer durationHours;
    private BigDecimal priceMxc;
    private List<String> features;
    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MentorPackageResponse fromEntity(MentorPackage entity) {
        MentorPackageResponse response = new MentorPackageResponse();
        response.setId(entity.getId());
        response.setMentorProfileId(entity.getMentorProfileId());
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setPackageType(entity.getPackageType());
        response.setDurationHours(entity.getDurationHours());
        response.setPriceMxc(entity.getPriceMxc());
        response.setFeatures(entity.getFeatures() != null ? Arrays.asList(entity.getFeatures()) : List.of());
        response.setIsActive(entity.getIsActive());
        response.setDisplayOrder(entity.getDisplayOrder());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
