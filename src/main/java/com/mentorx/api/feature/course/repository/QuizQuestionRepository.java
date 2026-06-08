package com.mentorx.api.feature.course.repository;

import com.mentorx.api.feature.course.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {
    List<QuizQuestion> findByLessonIdOrderByOrderIndexAsc(UUID lessonId);
}
