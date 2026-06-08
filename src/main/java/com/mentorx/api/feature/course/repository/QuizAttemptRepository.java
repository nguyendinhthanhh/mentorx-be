package com.mentorx.api.feature.course.repository;

import com.mentorx.api.feature.course.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    List<QuizAttempt> findByEnrollmentIdAndLessonIdOrderByStartedAtDesc(UUID enrollmentId, UUID lessonId);
}
