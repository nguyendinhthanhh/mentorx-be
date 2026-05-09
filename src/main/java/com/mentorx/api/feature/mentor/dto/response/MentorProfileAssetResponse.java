package com.mentorx.api.feature.mentor.dto.response;

import com.mentorx.api.feature.mentor.entity.MentorProfileAsset;
import com.mentorx.api.feature.mentor.enums.MentorProfileAssetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorProfileAssetResponse {

    private UUID id;
    private UUID mentorProfileId;
    private MentorProfileAssetType type;
    private String title;
    private String description;
    private String issuer;
    private String fileUrl;
    private String iconUrl;
    private LocalDate issuedAt;
    private Boolean isFeatured;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MentorProfileAssetResponse fromEntity(MentorProfileAsset entity) {
        return new MentorProfileAssetResponse(
                entity.getId(),
                entity.getMentorProfileId(),
                entity.getType(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getIssuer(),
                entity.getFileUrl(),
                entity.getIconUrl(),
                entity.getIssuedAt(),
                entity.getIsFeatured(),
                entity.getDisplayOrder(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
