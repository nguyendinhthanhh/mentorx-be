package com.mentorx.api.feature.analytics.entity;

import com.mentorx.api.feature.course.entity.Course;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Per-course daily snapshot. Mirrors {@link EarningsDailySnapshot} but at course granularity,
 * chosen for clean normalization (DEC-004 Option B) and a single dedicated aggregation step.
 */
@Entity
@Table(name = "course_daily_snapshots", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"course_id", "snapshot_date"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDailySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "enrollments_count", nullable = false)
    @Builder.Default
    private Integer enrollmentsCount = 0;

    @Column(name = "sold_count", nullable = false)
    @Builder.Default
    private Integer soldCount = 0;

    @Column(name = "revenue_mxc", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal revenueMxc = BigDecimal.ZERO;

    @Column(name = "completion_rate", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal completionRate = BigDecimal.ZERO;

    @Column(name = "lesson_views", nullable = false)
    @Builder.Default
    private Integer lessonViews = 0;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
