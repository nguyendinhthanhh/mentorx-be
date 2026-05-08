package com.mentorx.api.feature.course.repository;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.feature.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    @Query("SELECT c FROM Course c WHERE c.deletedAt IS NULL " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:instructorId IS NULL OR c.instructor.id = :instructorId) " +
           "AND (:categoryId IS NULL OR c.categoryId = :categoryId)")
    Page<Course> findAllWithFilters(@Param("status") CourseStatus status,
                                    @Param("instructorId") UUID instructorId,
                                    @Param("categoryId") Integer categoryId,
                                    Pageable pageable);

    Page<Course> findByStatusAndDeletedAtIsNull(CourseStatus status, Pageable pageable);
    Page<Course> findByInstructorIdAndDeletedAtIsNull(UUID instructorId, Pageable pageable);
    Optional<Course> findBySlugAndDeletedAtIsNull(String slug);
}
