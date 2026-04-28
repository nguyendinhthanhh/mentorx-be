package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_views")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobView extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "viewed_at", nullable = false)
    @Builder.Default
    private LocalDateTime viewedAt = LocalDateTime.now();
}