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
                    "LEFT JOIN course_skill_ids csi ON csi.course_id = c.id " +
                    "LEFT JOIN skills s ON s.id = csi.skill_id " +
                    "WHERE c.deleted_at IS NULL " +
                    "AND (CAST(:status AS varchar) IS NULL OR c.status = CAST(:status AS varchar)) " +
                    "AND (CAST(:instructorId AS uuid) IS NULL OR c.instructor_id = CAST(:instructorId AS uuid)) " +
                    "AND (CAST(:categoryId AS integer) IS NULL OR c.category_id = CAST(:categoryId AS integer)) " +
                    "AND (CAST(:language AS varchar) IS NULL OR c.language = CAST(:language AS varchar)) " +
                    "AND (CAST(:levelKeyword AS varchar) IS NULL OR LOWER(c.level) LIKE LOWER(CONCAT('%', CAST(:levelKeyword AS varchar), '%'))) " +
                    "AND (CAST(:skillKeyword AS varchar) IS NULL OR LOWER(cs.skill) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')) " +
                    "OR LOWER(s.slug) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')) " +
                    "OR LOWER(s.label_en) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')) " +
                    "OR LOWER(s.label_vi) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')))",
            countQuery = "SELECT COUNT(DISTINCT c.id) " +
                    "FROM courses c " +
                    "LEFT JOIN course_skills cs ON cs.course_id = c.id " +
                    "LEFT JOIN course_skill_ids csi ON csi.course_id = c.id " +
                    "LEFT JOIN skills s ON s.id = csi.skill_id " +
                    "WHERE c.deleted_at IS NULL " +
                    "AND (CAST(:status AS varchar) IS NULL OR c.status = CAST(:status AS varchar)) " +
                    "AND (CAST(:instructorId AS uuid) IS NULL OR c.instructor_id = CAST(:instructorId AS uuid)) " +
                    "AND (CAST(:categoryId AS integer) IS NULL OR c.category_id = CAST(:categoryId AS integer)) " +
                    "AND (CAST(:language AS varchar) IS NULL OR c.language = CAST(:language AS varchar)) " +
                    "AND (CAST(:levelKeyword AS varchar) IS NULL OR LOWER(c.level) LIKE LOWER(CONCAT('%', CAST(:levelKeyword AS varchar), '%'))) " +
                    "AND (CAST(:skillKeyword AS varchar) IS NULL OR LOWER(cs.skill) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')) " +
                    "OR LOWER(s.slug) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')) " +
                    "OR LOWER(s.label_en) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')) " +
                    "OR LOWER(s.label_vi) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')))",
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
                    "LEFT JOIN course_skill_ids csi ON csi.course_id = c.id " +
                    "LEFT JOIN skills s ON s.id = csi.skill_id " +
                    "WHERE c.deleted_at IS NULL " +
                    "AND c.status = CAST(:status AS varchar) " +
                    "AND (CAST(:categoryId AS integer) IS NULL OR c.category_id = CAST(:categoryId AS integer)) " +
                    "AND (CAST(:language AS varchar) IS NULL OR c.language = CAST(:language AS varchar)) " +
                    "AND (CAST(:levelKeyword AS varchar) IS NULL OR LOWER(c.level) LIKE LOWER(CONCAT('%', CAST(:levelKeyword AS varchar), '%'))) " +
                    "AND (CAST(:skillKeyword AS varchar) IS NULL OR LOWER(cs.skill) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')) " +
                    "OR LOWER(s.slug) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')) " +
                    "OR LOWER(s.label_en) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')) " +
                    "OR LOWER(s.label_vi) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')))",
            countQuery = "SELECT COUNT(DISTINCT c.id) " +
                    "FROM courses c " +
                    "LEFT JOIN course_skills cs ON cs.course_id = c.id " +
                    "LEFT JOIN course_skill_ids csi ON csi.course_id = c.id " +
                    "LEFT JOIN skills s ON s.id = csi.skill_id " +
                    "WHERE c.deleted_at IS NULL " +
                    "AND c.status = CAST(:status AS varchar) " +
                    "AND (CAST(:categoryId AS integer) IS NULL OR c.category_id = CAST(:categoryId AS integer)) " +
                    "AND (CAST(:language AS varchar) IS NULL OR c.language = CAST(:language AS varchar)) " +
                    "AND (CAST(:levelKeyword AS varchar) IS NULL OR LOWER(c.level) LIKE LOWER(CONCAT('%', CAST(:levelKeyword AS varchar), '%'))) " +
                    "AND (CAST(:skillKeyword AS varchar) IS NULL OR LOWER(cs.skill) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')) " +
                    "OR LOWER(s.slug) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')) " +
                    "OR LOWER(s.label_en) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')) " +
                    "OR LOWER(s.label_vi) LIKE LOWER(CONCAT('%', CAST(:skillKeyword AS varchar), '%')))",
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
