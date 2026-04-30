package com.mentorx.api.feature.course.repository;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.feature.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    Page<Course> findByStatusAndDeletedAtIsNull(CourseStatus status, Pageable pageable);
    Page<Course> findByInstructorIdAndDeletedAtIsNull(UUID instructorId, Pageable pageable);
    Optional<Course> findBySlugAndDeletedAtIsNull(String slug);
}
