package com.mentorx.api.feature.mentor.entity;

import com.mentorx.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mentor_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorSkill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_profile_id", nullable = false)
    private MentorProfile mentorProfile;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(name = "proficiency_level", nullable = false)
    private Integer proficiencyLevel; // 1-5 scale

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;
}