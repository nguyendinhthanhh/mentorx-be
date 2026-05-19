package com.mentorx.api.feature.course.repository;

import com.mentorx.api.feature.course.entity.CourseEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    
    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.student.id = :studentId")
    Long countByStudentId(@Param("studentId") UUID studentId);
    
    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.course.instructor.id = :instructorId")
    Long countByInstructorId(@Param("instructorId") UUID instructorId);
    
    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.course.instructor.id = :instructorId")
    Page<CourseEnrollment> findByInstructorId(@Param("instructorId") UUID instructorId, Pageable pageable);
    
    boolean existsByCourseIdAndStudentId(UUID courseId, UUID studentId);
    
    @Query("SELECT AVG(ce.progressPercent) FROM CourseEnrollment ce WHERE ce.course.id = :courseId")
    Double getAverageProgressByCourseId(@Param("courseId") UUID courseId);
}
