package com.mentorx.api.feature.matching.entity;

import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mentor_job_notification_settings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorJobNotificationSetting {

    @Id
    @Column(name = "user_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "notify_new_jobs", nullable = false)
    @Builder.Default
    private Boolean notifyNewJobs = true;

    @Column(name = "min_match_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal minMatchScore = new BigDecimal("0.50");

    @Column(name = "notify_category_ids")
    private List<Integer> notifyCategoryIds;

    @Column(name = "notify_frequency", nullable = false, length = 20)
    @Builder.Default
    private String notifyFrequency = "INSTANT";

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
