package com.mentorx.api.feature.mentor.entity;

import com.mentorx.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "mentor_availability")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorAvailability extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_profile_id", nullable = false)
    private MentorProfile mentorProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;
}