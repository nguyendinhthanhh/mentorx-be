package com.mentorx.api.feature.matching.entity;

import com.mentorx.api.feature.job.entity.Job;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_relevance_compute_queue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRelevanceComputeQueue {

    @Id
    @Column(name = "job_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "job_id")
    private Job job;

    @Column(name = "queued_at", nullable = false)
    @Builder.Default
    private LocalDateTime queuedAt = LocalDateTime.now();

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";
}
