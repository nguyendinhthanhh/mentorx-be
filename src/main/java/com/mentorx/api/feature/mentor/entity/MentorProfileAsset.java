package com.mentorx.api.feature.mentor.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.mentor.enums.MentorProfileAssetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "mentor_profile_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorProfileAsset extends BaseEntity {

    @Column(name = "mentor_profile_id", nullable = false)
    private UUID mentorProfileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 30)
    private MentorProfileAssetType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 150)
    private String issuer;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "issued_at")
    private LocalDate issuedAt;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
}
