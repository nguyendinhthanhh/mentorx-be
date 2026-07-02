package com.mentorx.api.feature.course.repository;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.feature.course.entity.CourseQaMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseQaMessageRepository extends JpaRepository<CourseQaMessage, UUID> {
    List<CourseQaMessage> findTop100ByCourseIdOrderByCreatedAtDesc(UUID courseId);

    List<CourseQaMessage> findByCourseIdOrderByCreatedAtAsc(UUID courseId);

    @Query("SELECT m FROM CourseQaMessage m " +
            "JOIN FETCH m.course c " +
            "JOIN FETCH m.sender s " +
            "LEFT JOIN FETCH m.recipient r " +
            "WHERE c.instructor.id = :mentorId " +
            "AND c.status = :status " +
            "AND c.deletedAt IS NULL " +
            "ORDER BY m.createdAt ASC")
    List<CourseQaMessage> findByMentorAndCourseStatus(@Param("mentorId") UUID mentorId,
                                                      @Param("status") CourseStatus status);
}
