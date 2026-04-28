package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_deliverables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDeliverable extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private JobContract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id")
    private JobMilestone milestone;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private Boolean isApproved = false;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "client_feedback", columnDefinition = "TEXT")
    private String clientFeedback;
}