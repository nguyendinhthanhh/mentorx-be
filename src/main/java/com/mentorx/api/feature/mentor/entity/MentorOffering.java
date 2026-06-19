package com.mentorx.api.feature.mentor.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.CourseLevel;
import com.mentorx.api.common.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "mentor_courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorOffering extends BaseEntity {

    @Column(name = "mentor_profile_id", nullable = false)
    private UUID mentorProfileId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_mxc", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceMxc;

    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CourseLevel level;

    @Column(name = "lessons_count", nullable = false)
    private Integer lessonsCount;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CourseStatus status = CourseStatus.PUBLISHED;
}

