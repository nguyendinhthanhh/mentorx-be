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
    @Query(
            value = "SELECT DISTINCT c.* " +
                    "FROM courses c " +
                    "LEFT JOIN course_skills cs ON cs.course_id = c.id " +
                    "WHERE c.deleted_at IS NULL " +
                    "AND (:status IS NULL OR c.status = CAST(:status AS varchar)) " +
                    "AND (:instructorId IS NULL OR c.instructor_id = :instructorId) " +
                    "AND (:categoryId IS NULL OR c.category_id = :categoryId) " +
                    "AND (:language IS NULL OR c.language = CAST(:language AS varchar)) " +
                    "AND (:levelKeyword IS NULL OR LOWER(c.level) LIKE LOWER(CONCAT('%', :levelKeyword, '%'))) " +
                    "AND (:skillKeyword IS NULL OR LOWER(cs.skill) LIKE LOWER(CONCAT('%', :skillKeyword, '%')))",
            countQuery = "SELECT COUNT(DISTINCT c.id) " +
                    "FROM courses c " +
                    "LEFT JOIN course_skills cs ON cs.course_id = c.id " +
                    "WHERE c.deleted_at IS NULL " +
                    "AND (:status IS NULL OR c.status = CAST(:status AS varchar)) " +
                    "AND (:instructorId IS NULL OR c.instructor_id = :instructorId) " +
                    "AND (:categoryId IS NULL OR c.category_id = :categoryId) " +
                    "AND (:language IS NULL OR c.language = CAST(:language AS varchar)) " +
                    "AND (:levelKeyword IS NULL OR LOWER(c.level) LIKE LOWER(CONCAT('%', :levelKeyword, '%'))) " +
                    "AND (:skillKeyword IS NULL OR LOWER(cs.skill) LIKE LOWER(CONCAT('%', :skillKeyword, '%')))",
            nativeQuery = true
    )
    Page<Course> findAllWithFilters(@Param("status") String status,
                                    @Param("instructorId") UUID instructorId,
                                    @Param("categoryId") Integer categoryId,
                                    @Param("language") String language,
                                    @Param("levelKeyword") String levelKeyword,
                                    @Param("skillKeyword") String skillKeyword,
                                    Pageable pageable);

    @Query(
            value = "SELECT DISTINCT c.* " +
                    "FROM courses c " +
                    "LEFT JOIN course_skills cs ON cs.course_id = c.id " +
                    "WHERE c.deleted_at IS NULL " +
                    "AND c.status = CAST(:status AS varchar) " +
                    "AND (:categoryId IS NULL OR c.category_id = :categoryId) " +
                    "AND (:language IS NULL OR c.language = CAST(:language AS varchar)) " +
                    "AND (:levelKeyword IS NULL OR LOWER(c.level) LIKE LOWER(CONCAT('%', :levelKeyword, '%'))) " +
                    "AND (:skillKeyword IS NULL OR LOWER(cs.skill) LIKE LOWER(CONCAT('%', :skillKeyword, '%')))",
            countQuery = "SELECT COUNT(DISTINCT c.id) " +
                    "FROM courses c " +
                    "LEFT JOIN course_skills cs ON cs.course_id = c.id " +
                    "WHERE c.deleted_at IS NULL " +
                    "AND c.status = CAST(:status AS varchar) " +
                    "AND (:categoryId IS NULL OR c.category_id = :categoryId) " +
                    "AND (:language IS NULL OR c.language = CAST(:language AS varchar)) " +
                    "AND (:levelKeyword IS NULL OR LOWER(c.level) LIKE LOWER(CONCAT('%', :levelKeyword, '%'))) " +
                    "AND (:skillKeyword IS NULL OR LOWER(cs.skill) LIKE LOWER(CONCAT('%', :skillKeyword, '%')))",
            nativeQuery = true
    )
    Page<Course> findPublishedWithFilters(@Param("status") String status,
                                          @Param("categoryId") Integer categoryId,
                                          @Param("language") String language,
                                          @Param("levelKeyword") String levelKeyword,
                                          @Param("skillKeyword") String skillKeyword,
                                          Pageable pageable);

    Page<Course> findByStatusAndDeletedAtIsNull(CourseStatus status, Pageable pageable);
    Page<Course> findByInstructorIdAndDeletedAtIsNull(UUID instructorId, Pageable pageable);
    Optional<Course> findBySlugAndDeletedAtIsNull(String slug);
}
