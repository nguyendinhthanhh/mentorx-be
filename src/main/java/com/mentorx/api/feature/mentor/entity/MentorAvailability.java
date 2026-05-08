package com.mentorx.api.feature.mentor.entity;

import com.mentorx.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "mentor_availability")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorAvailability extends BaseEntity {

    @Column(name = "mentor_profile_id", nullable = false)
    private UUID mentorProfileId;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1 = Monday, 7 = Sunday

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
