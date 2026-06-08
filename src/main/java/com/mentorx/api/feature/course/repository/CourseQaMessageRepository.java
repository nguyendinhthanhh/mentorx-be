package com.mentorx.api.feature.course.repository;

import com.mentorx.api.feature.course.entity.CourseQaMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseQaMessageRepository extends JpaRepository<CourseQaMessage, UUID> {
    List<CourseQaMessage> findTop100ByCourseIdOrderByCreatedAtDesc(UUID courseId);
}
