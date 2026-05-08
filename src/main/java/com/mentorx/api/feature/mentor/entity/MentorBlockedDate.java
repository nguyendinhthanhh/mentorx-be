package com.mentorx.api.feature.mentor.entity;

import com.mentorx.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "mentor_blocked_dates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorBlockedDate extends BaseEntity {

    @Column(name = "mentor_profile_id", nullable = false)
    private UUID mentorProfileId;

    @Column(name = "blocked_date", nullable = false)
    private LocalDate blockedDate;

    @Column(length = 500)
    private String reason;
}
