package com.mentorx.api.feature.course.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonProgress {

    @EmbeddedId
    private LessonProgressId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("enrollmentId")
    @JoinColumn(name = "enrollment_id")
    private CourseEnrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("lessonId")
    @JoinColumn(name = "lesson_id")
    private CourseLesson lesson;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "watch_duration_sec")
    @Builder.Default
    private Integer watchDurationSec = 0;
}
