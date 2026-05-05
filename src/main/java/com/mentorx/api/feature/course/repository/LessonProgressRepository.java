package com.mentorx.api.feature.course.repository;

import com.mentorx.api.feature.course.entity.LessonProgress;
import com.mentorx.api.feature.course.entity.LessonProgressId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, LessonProgressId> {
    
    List<LessonProgress> findByIdEnrollmentId(UUID enrollmentId);
    
    Optional<LessonProgress> findByIdEnrollmentIdAndIdLessonId(UUID enrollmentId, UUID lessonId);
    
    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.id.enrollmentId = :enrollmentId AND lp.isCompleted = true")
    Long countCompletedLessonsByEnrollmentId(@Param("enrollmentId") UUID enrollmentId);
    
    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.id.enrollmentId = :enrollmentId")
    Long countTotalLessonsByEnrollmentId(@Param("enrollmentId") UUID enrollmentId);
    
    @Query("SELECT lp FROM LessonProgress lp WHERE lp.enrollment.student.id = :studentId AND lp.lesson.section.course.id = :courseId")
    List<LessonProgress> findByStudentIdAndCourseId(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);
    
    boolean existsByIdEnrollmentIdAndIdLessonId(UUID enrollmentId, UUID lessonId);
}
