package com.mentorx.api.feature.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonProgressId implements Serializable {

    @Column(name = "enrollment_id")
    private UUID enrollmentId;

    @Column(name = "lesson_id")
    private UUID lessonId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LessonProgressId that = (LessonProgressId) o;
        return Objects.equals(enrollmentId, that.enrollmentId) &&
                Objects.equals(lessonId, that.lessonId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId, lessonId);
    }
}
