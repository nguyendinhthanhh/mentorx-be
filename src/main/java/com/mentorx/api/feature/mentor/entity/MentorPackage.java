package com.mentorx.api.feature.mentor.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.PackageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "mentor_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorPackage extends BaseEntity {

    @Column(name = "mentor_profile_id", nullable = false)
    private UUID mentorProfileId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_type", nullable = false, length = 50)
    private PackageType packageType;

    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours;

    @Column(name = "price_mxc", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceMxc;

    @Column(columnDefinition = "TEXT[]")
    private String[] features;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
}
