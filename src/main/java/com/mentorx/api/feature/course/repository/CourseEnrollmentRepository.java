package com.mentorx.api.feature.course.repository;

import com.mentorx.api.feature.course.entity.CourseEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, UUID> {
    
    Optional<CourseEnrollment> findByCourseIdAndStudentId(UUID courseId, UUID studentId);
    
    Page<CourseEnrollment> findByStudentId(UUID studentId, Pageable pageable);
    
    Page<CourseEnrollment> findByCourseId(UUID courseId, Pageable pageable);
    
    List<CourseEnrollment> findByStudentIdAndIsCompleted(UUID studentId, Boolean isCompleted);
    
    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.course.id = :courseId")
    Long countByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.course.id = :courseId AND ce.isCompleted = true")
    Long countCompletedByCourseId(@Param("courseId") UUID courseId);
    
    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.student.id = :studentId")
    Long countByStudentId(@Param("studentId") UUID studentId);
    
    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.course.instructor.id = :instructorId")
    Long countByInstructorId(@Param("instructorId") UUID instructorId);
    
    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.course.instructor.id = :instructorId")
    Page<CourseEnrollment> findByInstructorId(@Param("instructorId") UUID instructorId, Pageable pageable);
    
    boolean existsByCourseIdAndStudentId(UUID courseId, UUID studentId);
    
    @Query("SELECT AVG(ce.progressPercent) FROM CourseEnrollment ce WHERE ce.course.id = :courseId")
    Double getAverageProgressByCourseId(@Param("courseId") UUID courseId);

    /**
     * M12.2 Phase H1.4: per-instructor enrollment count within a time window.
     * Replaces the previous BUG-D behavior in
     * {@code EarningsAggregationJob.aggregateEnrollmentsByInstructor} which
     * called {@code revenueByCourseInWindow} + filtered {@code countEnrollmentsByCourseInWindow}
     * in an O(N×K) nested loop. This direct GROUP BY instructor collapses the work to O(N).
     */
    @Query("SELECT ce.course.instructor.id, COUNT(ce) FROM CourseEnrollment ce " +
           "WHERE ce.enrolledAt >= :start AND ce.enrolledAt < :end " +
           "AND ce.course.instructor IS NOT NULL " +
           "GROUP BY ce.course.instructor.id")
    List<Object[]> countEnrollmentsByInstructorInWindow(@Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end);

    // M12.2 H0: required by EarningsAggregationJob.aggregateCourseSnapshots
    @Query("SELECT ce.course.id, COUNT(ce) FROM CourseEnrollment ce " +
           "WHERE ce.enrolledAt >= :start AND ce.enrolledAt < :end " +
           "GROUP BY ce.course.id")
    List<Object[]> countEnrollmentsByCourseInWindow(@Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    @Query("SELECT ce.course.id, ce.course.instructor.id, COALESCE(SUM(ce.amountPaidMxc), 0) FROM CourseEnrollment ce " +
           "WHERE ce.enrolledAt >= :start AND ce.enrolledAt < :end " +
           "GROUP BY ce.course.id, ce.course.instructor.id")
    List<Object[]> revenueByCourseInWindow(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    // M12.2 H0 / H2.4: required by CourseStatsServiceImpl.sumRevenueForCourse
    @Query("SELECT COALESCE(SUM(ce.amountPaidMxc), 0) FROM CourseEnrollment ce WHERE ce.course.id = :courseId")
    java.math.BigDecimal sumRevenueByCourseId(@Param("courseId") UUID courseId);
}
